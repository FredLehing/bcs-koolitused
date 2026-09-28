<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import TrainingTranslationService from '@/api-services/TrainingTranslationService.js'
import LanguageService from '@/api-services/LanguageService.js'
import CategoryService from '@/api-services/CategoryService.js'
import FundingTypeService from '@/api-services/FundingTypeService.js'
import LocationService from '@/api-services/LocationService.js'
import LecturerService from '@/api-services/LecturerService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'
import AlertSuccess from '@/components/common/AlertSuccess.vue'
import TranslationFlags from '@/components/common/TranslationFlags.vue'
import TrainingDataForm from '@/components/forms/TrainingDataForm.vue'
import TrainingTranslationForm from '@/components/forms/TrainingTranslationForm.vue'
import LecturerSelectModal from '@/components/modals/LecturerSelectModal.vue'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Vaate olekud (state) tuletatakse URL-i query parameetritest:
//   new-training     /training-form
//   update           /training-form?trainingId={id}&trainingTranslationId={id}
//   new-translation  /training-form?trainingId={id}&languageId={id}
const STATE_NEW_TRAINING = 'new-training'
const STATE_UPDATE = 'update'
const STATE_NEW_TRANSLATION = 'new-translation'

export default {
  name: 'TrainingFormView',
  components: {
    ConfirmModal,
    LecturerSelectModal,
    TrainingTranslationForm,
    TrainingDataForm,
    TranslationFlags,
    AlertSuccess,
    AlertDanger,
  },
  data() {
    return {
      state: STATE_NEW_TRAINING,
      successMessage: '',
      errorMessage: '',

      trainingId: 0,
      trainingTranslationId: 0,
      targetLanguageId: 0,

      languages: [],
      categories: [],
      fundingTypes: [],
      locations: [],
      lecturers: [],
      trainingTranslations: [],

      training: {
        trainingId: 0,
        categoryId: 0,
        trainingLanguageId: 0,
        locationId: 0,
        defaultLecturerId: null,
        defaultLecturerName: null,
        isOrderable: false,
        isPromoted: false,
        status: '',
        fundingTypeIds: [],
      },

      translation: {
        trainingTranslationId: 0,
        languageId: 0,
        languageCode: '',
        title: '',
        shortDescription: '',
        description: '',
      },

      // Viimati laaditud/salvestatud tõlketekstid — salvestamata muudatuste tuvastamiseks
      savedTranslationTexts: '',

      errorResponse: {
        message: '',
        errorCode: '',
      },

      isLecturerModalOpen: false,
      isStatusModalOpen: false,
      isAiConfirmModalOpen: false,
      isAiLoading: false,
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLanguages', 'mainLanguageCode']),

    pageTitle() {
      if (this.state === STATE_NEW_TRAINING) {
        return 'Lisa uus koolitus'
      } else if (this.state === STATE_NEW_TRANSLATION) {
        return 'Lisa koolituse tõlge'
      }
      return 'Muuda koolitust'
    },

    isNewTraining() {
      return this.state === STATE_NEW_TRAINING
    },

    isUpdate() {
      return this.state === STATE_UPDATE
    },

    isNewTranslation() {
      return this.state === STATE_NEW_TRANSLATION
    },

    isPublished() {
      return this.training.status === 'P'
    },

    translationLanguageName() {
      const language = this.languages.find(
        (language) => language.languageCode === this.translation.languageCode,
      )
      return language ? language.languageName : this.translation.languageCode
    },

    showAiButton() {
      return (
        this.isNewTranslation ||
        (this.isUpdate && this.translation.languageCode !== this.mainLanguageCode)
      )
    },

    aiTooltip() {
      const saveButton = this.isNewTranslation ? 'Lisa tõlge' : 'Salvesta'
      return (
        'Tõlge tehakse salvestatud põhikeele (' +
        this.mainLanguageCode +
        ') tekstist, mitte vormi sisust. Tulemus kuvatakse ainult vormis — see salvestub alles siis, kui vajutad „' +
        saveButton +
        '“.'
      )
    },

    statusModalTitle() {
      return this.isPublished ? 'Liiguta mustandisse' : 'Publitseeri koolitus'
    },

    statusModalMessage() {
      return this.isPublished
        ? 'Kas soovid koolituse mustandisse liigutada? See kaob avalikust nimekirjast.'
        : 'Kas soovid koolituse publitseerida? See muutub avalikult nähtavaks.'
    },
  },
  watch: {
    // Iga router.replace (nt "Lisa", lipule klikk) muudab query parameetreid → laadi andmed uuesti
    '$route.query'() {
      this.loadView()
    },
  },
  methods: {
    loadView() {
      this.trainingId = Number(this.$route.query.trainingId ?? 0)
      this.trainingTranslationId = Number(this.$route.query.trainingTranslationId ?? 0)
      this.targetLanguageId = Number(this.$route.query.languageId ?? 0)
      this.state = this.resolveState()
      this.getLocations()
      this.getLanguages()
    },

    resolveState() {
      if (this.trainingId === 0) {
        return STATE_NEW_TRAINING
      } else if (this.trainingTranslationId !== 0) {
        return STATE_UPDATE
      }
      return STATE_NEW_TRANSLATION
    },

    // Oleku andmed laaditakse pärast keelte nimekirja, sest languageId ↔ languageCode teisendus vajab seda
    loadStateData() {
      if (this.isNewTraining) {
        this.loadNewTrainingState()
      } else if (this.isUpdate) {
        this.loadUpdateState()
      } else {
        this.loadNewTranslationState()
      }
    },

    loadNewTrainingState() {
      this.resetTraining()
      this.resetTranslation()
      this.trainingTranslations = []
      this.translation.languageCode = this.mainLanguageCode
      this.savedTranslationTexts = this.getTranslationTexts()
      this.getDropdownsInLanguage(this.mainLanguageCode)
    },

    loadUpdateState() {
      this.getTraining()
      this.getTrainingTranslations()
      TrainingTranslationService.sendGetTrainingTranslationRequest(this.trainingTranslationId)
        .then((response) => this.handleGetUpdateTranslationResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetUpdateTranslationResponse(translation) {
      this.translation = translation
      this.savedTranslationTexts = this.getTranslationTexts()
      this.getDropdownsInLanguage(translation.languageCode)
    },

    loadNewTranslationState() {
      const targetLanguage = this.languages.find(
        (language) => language.languageId === this.targetLanguageId,
      )
      this.resetTranslation()
      this.translation.languageId = this.targetLanguageId
      this.translation.languageCode = targetLanguage ? targetLanguage.languageCode : ''
      this.getTraining()
      this.getDropdownsInLanguage(this.translation.languageCode)
      TrainingService.sendGetTrainingTranslationsRequest(this.trainingId)
        .then((response) => this.handleGetNewTranslationTrainingTranslationsResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetNewTranslationTrainingTranslationsResponse(trainingTranslations) {
      this.trainingTranslations = trainingTranslations
      const mainTranslation = trainingTranslations.find(
        (trainingTranslation) => trainingTranslation.isMainLanguage,
      )
      TrainingTranslationService.sendGetTrainingTranslationRequest(
        mainTranslation.trainingTranslationId,
      )
        .then((response) => this.handleGetMainTranslationResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Uue tõlke väljad eeltäidetakse salvestatud põhikeele tekstiga
    handleGetMainTranslationResponse(mainTranslation) {
      this.translation.title = mainTranslation.title
      this.translation.shortDescription = mainTranslation.shortDescription
      this.translation.description = mainTranslation.description
      this.savedTranslationTexts = this.getTranslationTexts()
    },

    getLanguages() {
      LanguageService.sendGetLanguagesRequest()
        .then((response) => this.handleGetLanguagesResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetLanguagesResponse(languages) {
      this.languages = languages
      this.loadStateData()
    },

    getLocations() {
      LocationService.sendGetLocationsRequest()
        .then((response) => (this.locations = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getDropdownsInLanguage(languageCode) {
      CategoryService.sendGetCategoriesRequest(languageCode)
        .then((response) => (this.categories = response.data))
        .catch(() => NavigationService.navigateToErrorView())
      FundingTypeService.sendGetFundingTypesRequest(languageCode)
        .then((response) => (this.fundingTypes = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getTraining() {
      TrainingService.sendGetTrainingRequest(this.trainingId)
        .then((response) => (this.training = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getTrainingTranslations() {
      TrainingService.sendGetTrainingTranslationsRequest(this.trainingId)
        .then((response) => (this.trainingTranslations = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // ---------- "Lisa" (new-training) ----------

    addTraining() {
      this.resetMessages()
      this.checkTrainingDataForErrors()
      this.checkTranslationForErrors()

      if (this.errorMessageIsEmpty()) {
        TrainingService.sendPostTrainingRequest(this.createTrainingCreateRequest())
          .then((response) => this.handleAddTrainingResponse(response.data))
          .catch(() => NavigationService.navigateToErrorView())
      }
    },

    createTrainingCreateRequest() {
      return {
        userId: SessionStorageService.getUserId(),
        categoryId: this.training.categoryId,
        trainingLanguageId: this.training.trainingLanguageId,
        locationId: this.training.locationId,
        defaultLecturerId: this.training.defaultLecturerId,
        isOrderable: this.training.isOrderable,
        isPromoted: this.training.isPromoted,
        fundingTypeIds: this.training.fundingTypeIds,
        title: this.translation.title,
        shortDescription: this.translation.shortDescription,
        description: this.translation.description,
      }
    },

    handleAddTrainingResponse(trainingCreateResponse) {
      this.successMessage = 'Koolitus "' + this.translation.title + '" on lisatud mustandina'
      NavigationService.replaceTrainingFormView({
        trainingId: trainingCreateResponse.trainingId,
        trainingTranslationId: trainingCreateResponse.trainingTranslationId,
      })
    },

    // ---------- "Salvesta" (update) ----------

    updateTraining() {
      this.resetMessages()
      this.checkTrainingDataForErrors()
      this.checkTranslationForErrors()

      if (this.errorMessageIsEmpty()) {
        TrainingService.sendPutTrainingRequest(this.trainingId, this.createTrainingUpdateRequest())
          .then(() => this.handleUpdateTrainingResponse())
          .catch(() => NavigationService.navigateToErrorView())
      }
    },

    createTrainingUpdateRequest() {
      return {
        categoryId: this.training.categoryId,
        trainingLanguageId: this.training.trainingLanguageId,
        locationId: this.training.locationId,
        defaultLecturerId: this.training.defaultLecturerId,
        isOrderable: this.training.isOrderable,
        isPromoted: this.training.isPromoted,
        fundingTypeIds: this.training.fundingTypeIds,
        trainingTranslationId: this.translation.trainingTranslationId,
        title: this.translation.title,
        shortDescription: this.translation.shortDescription,
        description: this.translation.description,
      }
    },

    handleUpdateTrainingResponse() {
      this.successMessage = 'Muudatused on salvestatud'
      this.savedTranslationTexts = this.getTranslationTexts()
    },

    // ---------- "Lisa tõlge" (new-translation) ----------

    addTrainingTranslation() {
      this.resetMessages()
      this.checkTranslationForErrors()

      if (this.errorMessageIsEmpty()) {
        TrainingService.sendPostTrainingTranslationRequest(this.trainingId, {
          languageId: this.targetLanguageId,
          title: this.translation.title,
          shortDescription: this.translation.shortDescription,
          description: this.translation.description,
        })
          .then((response) => this.handleAddTrainingTranslationResponse(response.data))
          .catch(() => NavigationService.navigateToErrorView())
      }
    },

    handleAddTrainingTranslationResponse(trainingTranslationCreateResponse) {
      this.successMessage = 'Tõlge (' + this.translation.languageCode + ') on lisatud'
      NavigationService.replaceTrainingFormView({
        trainingId: this.trainingId,
        trainingTranslationId: trainingTranslationCreateResponse.trainingTranslationId,
      })
    },

    // ---------- Lipukesed ----------

    handleTranslationFlagClicked(languageCode) {
      if (this.isNewTraining || languageCode === this.translation.languageCode) {
        return
      }
      this.resetMessages()
      const existingTranslation = this.trainingTranslations.find(
        (trainingTranslation) => trainingTranslation.languageCode === languageCode,
      )

      if (existingTranslation) {
        NavigationService.replaceTrainingFormView({
          trainingId: this.trainingId,
          trainingTranslationId: existingTranslation.trainingTranslationId,
        })
      } else {
        this.openNewTranslation(languageCode)
      }
    },

    openNewTranslation(languageCode) {
      const language = this.languages.find((language) => language.languageCode === languageCode)

      if (language) {
        NavigationService.replaceTrainingFormView({
          trainingId: this.trainingId,
          languageId: language.languageId,
        })
      } else {
        this.errorMessage = 'Keelt "' + languageCode + '" ei ole süsteemis'
      }
    },

    // ---------- Publitseeri / Liiguta mustandisse ----------

    changeTrainingStatus() {
      this.isStatusModalOpen = false
      this.resetMessages()
      const request = this.isPublished
        ? TrainingService.sendPutTrainingUnpublishRequest(this.trainingId)
        : TrainingService.sendPutTrainingPublishRequest(this.trainingId)
      request
        .then(() => this.handleChangeTrainingStatusResponse())
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleChangeTrainingStatusResponse() {
      this.successMessage = this.isPublished
        ? 'Koolitus on liigutatud mustandisse'
        : 'Koolitus on publitseeritud'
      this.getTraining()
    },

    // ---------- Lektori valik ----------

    openLecturerModal() {
      this.isLecturerModalOpen = true
      this.searchLecturers('')
    },

    searchLecturers(search) {
      LecturerService.sendGetLecturersRequest(search)
        .then((response) => (this.lecturers = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleLecturerSelected(lecturer) {
      this.training.defaultLecturerId = lecturer ? lecturer.lecturerId : null
      this.training.defaultLecturerName = lecturer ? lecturer.lecturerName : null
      this.isLecturerModalOpen = false
    },

    // ---------- AI tõlge ----------

    handleAiTranslationClicked() {
      if (this.translationHasUnsavedChanges()) {
        this.isAiConfirmModalOpen = true
      } else {
        this.getAiTranslation()
      }
    },

    getAiTranslation() {
      this.isAiConfirmModalOpen = false
      this.resetMessages()
      this.isAiLoading = true
      TrainingService.sendGetAiTranslationRequest(this.trainingId, this.translation.languageId)
        .then((response) => this.handleGetAiTranslationResponse(response.data))
        .catch((error) => this.handleGetAiTranslationError(error))
        .finally(() => (this.isAiLoading = false))
    },

    // AI tulemus kuvatakse ainult vormis — andmebaasi läheb see "Lisa tõlge" / "Salvesta" nupuga
    handleGetAiTranslationResponse(aiTranslation) {
      this.translation.title = aiTranslation.title
      this.translation.shortDescription = aiTranslation.shortDescription
      this.translation.description = aiTranslation.description
      this.successMessage = 'AI tõlge on vormis — kontrolli teksti ja salvesta'
    },

    // AI teenuse teadaolevad vead kuvatakse vormis — admini sisestatud tekst jääb alles
    handleGetAiTranslationError(error) {
      const statusCode = error.response?.status
      this.errorResponse = error.response?.data ?? { message: '', errorCode: '' }

      if (
        (statusCode === 503 && this.errorResponse.errorCode === 'AI_SERVICE_UNAVAILABLE') ||
        (statusCode === 403 && this.errorResponse.errorCode === 'MAIN_LANGUAGE_NOT_TRANSLATABLE') ||
        (statusCode === 404 && this.errorResponse.errorCode === 'MAIN_TRANSLATION_NOT_FOUND')
      ) {
        this.errorMessage = this.errorResponse.message
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    translationHasUnsavedChanges() {
      return this.getTranslationTexts() !== this.savedTranslationTexts
    },

    getTranslationTexts() {
      return (
        this.translation.title +
        '|' +
        this.translation.shortDescription +
        '|' +
        this.translation.description
      )
    },

    // ---------- Vormi väljad ----------

    updateFundingTypeIds(updatedCheckbox) {
      const otherFundingTypeIds = this.training.fundingTypeIds.filter(
        (fundingTypeId) => fundingTypeId !== updatedCheckbox.fundingTypeId,
      )
      this.training.fundingTypeIds = updatedCheckbox.checked
        ? [...otherFundingTypeIds, updatedCheckbox.fundingTypeId]
        : otherFundingTypeIds
    },

    checkTrainingDataForErrors() {
      if (this.training.categoryId === 0) {
        this.errorMessage = 'Vali kategooria'
      } else if (this.training.trainingLanguageId === 0) {
        this.errorMessage = 'Vali koolituse keel'
      } else if (this.training.locationId === 0) {
        this.errorMessage = 'Vali toimumiskoht'
      }
    },

    checkTranslationForErrors() {
      if (!this.errorMessageIsEmpty()) {
        return
      }
      if (this.translation.title.trim() === '') {
        this.errorMessage = 'Lisa pealkiri'
      } else if (this.translation.shortDescription.trim() === '') {
        this.errorMessage = 'Lisa lühikirjeldus'
      } else if (this.translation.description.trim() === '') {
        this.errorMessage = 'Lisa kirjeldus'
      }
    },

    errorMessageIsEmpty() {
      return this.errorMessage === ''
    },

    resetTraining() {
      this.training = {
        trainingId: 0,
        categoryId: 0,
        trainingLanguageId: 0,
        locationId: 0,
        defaultLecturerId: null,
        defaultLecturerName: null,
        isOrderable: false,
        isPromoted: false,
        status: '',
        fundingTypeIds: [],
      }
    },

    resetTranslation() {
      this.translation = {
        trainingTranslationId: 0,
        languageId: 0,
        languageCode: '',
        title: '',
        shortDescription: '',
        description: '',
      }
    },

    resetMessages() {
      this.successMessage = ''
      this.errorMessage = ''
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.loadView()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
  },
}
</script>

<template>
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-lg-10">
        <AlertSuccess :success-message="successMessage" />
        <AlertDanger :error-message="errorMessage" />

        <div class="d-flex flex-wrap align-items-center gap-3 mb-4">
          <h1 class="mb-0">{{ pageTitle }}</h1>
          <span
            v-if="!isNewTraining && training.status !== ''"
            class="badge"
            :class="isPublished ? 'text-bg-success' : 'text-bg-secondary'"
          >
            {{ isPublished ? 'Publitseeritud' : 'Mustand' }}
          </span>
          <TranslationFlags
            v-if="!isNewTraining"
            class="ms-auto"
            :content-languages="contentLanguages"
            :training-translations="trainingTranslations"
            :current-language-code="translation.languageCode"
            @event-translation-flag-clicked="handleTranslationFlagClicked"
          />
        </div>

        <TrainingDataForm
          :training="training"
          :categories="categories"
          :languages="languages"
          :locations="locations"
          :funding-types="fundingTypes"
          :is-disabled="isNewTranslation"
          @event-new-category-selected="training.categoryId = $event"
          @event-new-training-language-selected="training.trainingLanguageId = $event"
          @event-new-location-selected="training.locationId = $event"
          @event-select-lecturer-clicked="openLecturerModal"
          @event-funding-type-checkbox-updated="updateFundingTypeIds"
          @event-is-orderable-changed="training.isOrderable = $event"
          @event-is-promoted-changed="training.isPromoted = $event"
        />

        <TrainingTranslationForm
          :translation="translation"
          :language-name="translationLanguageName"
          :show-ai-button="showAiButton"
          :is-ai-loading="isAiLoading"
          :ai-tooltip="aiTooltip"
          @event-new-title-input="translation.title = $event"
          @event-new-short-description-input="translation.shortDescription = $event"
          @event-new-description-input="translation.description = $event"
          @event-ai-translation-clicked="handleAiTranslationClicked"
        />

        <div class="d-flex flex-wrap gap-3 mb-5">
          <button v-if="isNewTraining" @click="addTraining" class="btn btn-success" type="button">
            Lisa
          </button>
          <button v-if="isUpdate" @click="updateTraining" class="btn btn-success" type="button">
            Salvesta
          </button>
          <button
            v-if="isNewTranslation"
            @click="addTrainingTranslation"
            class="btn btn-success"
            type="button"
          >
            Lisa tõlge
          </button>
          <button
            v-if="!isNewTraining"
            @click="isStatusModalOpen = true"
            class="btn btn-outline-primary"
            type="button"
          >
            {{ isPublished ? 'Liiguta mustandisse' : 'Publitseeri' }}
          </button>
        </div>
      </div>
    </div>

    <LecturerSelectModal
      :is-open="isLecturerModalOpen"
      :lecturers="lecturers"
      @event-lecturer-search="searchLecturers"
      @event-lecturer-selected="handleLecturerSelected"
      @event-modal-closed="isLecturerModalOpen = false"
    />

    <ConfirmModal
      :is-open="isStatusModalOpen"
      :title="statusModalTitle"
      :message="statusModalMessage"
      :confirm-label="statusModalTitle"
      @event-confirmed="changeTrainingStatus"
      @event-modal-closed="isStatusModalOpen = false"
    />

    <ConfirmModal
      :is-open="isAiConfirmModalOpen"
      title="Asenda tekst AI tõlkega?"
      message="Vormis on salvestamata muudatusi. AI tõlge kirjutab pealkirja, lühikirjelduse ja kirjelduse üle."
      confirm-label="Asenda"
      @event-confirmed="getAiTranslation"
      @event-modal-closed="isAiConfirmModalOpen = false"
    />
  </div>
</template>
