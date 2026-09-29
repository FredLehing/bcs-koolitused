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
    ...mapState(useLanguageStore, ['contentLang']),

    // Keeled, millesse koolituse sisu tõlgitakse (lipukesed); õppekeeled nagu ru siia ei kuulu
    translationLanguages() {
      return this.languages.filter((language) => language.requiresTranslation)
    },

    mainLanguageCode() {
      const mainLanguage = this.languages.find((language) => language.isMainLanguage)
      return mainLanguage ? mainLanguage.languageCode : ''
    },

    pageTitle() {
      if (this.state === STATE_NEW_TRAINING) {
        return this.$t('trainingForm.title.newTraining')
      } else if (this.state === STATE_NEW_TRANSLATION) {
        return this.$t('trainingForm.title.newTranslation')
      }
      return this.$t('trainingForm.title.update')
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
      const saveButton = this.isNewTranslation
        ? this.$t('trainingForm.buttons.addTranslation')
        : this.$t('trainingForm.buttons.save')
      return this.$t('trainingForm.translation.aiTooltip', {
        mainLanguage: this.mainLanguageCode,
        saveButton: saveButton,
      })
    },

    statusModalTitle() {
      return this.isPublished
        ? this.$t('trainingForm.statusModal.unpublishTitle')
        : this.$t('trainingForm.statusModal.publishTitle')
    },

    statusModalMessage() {
      return this.isPublished
        ? this.$t('trainingForm.statusModal.unpublishMessage')
        : this.$t('trainingForm.statusModal.publishMessage')
    },
  },
  watch: {
    // Iga router.replace (nt "Lisa", lipule klikk) muudab query parameetreid → laadi andmed uuesti
    '$route.query'() {
      this.loadView()
    },

    // Kasutajaliidese keele vahetus (navbar) → laadi rippmenüüde tõlgitud väärtused uuesti.
    // Vormi sisu (koolituse andmed, tõlke tekst) jääb puutumata.
    contentLang() {
      this.getDropdowns()
    },
  },
  methods: {
    loadView() {
      this.trainingId = Number(this.$route.query.trainingId ?? 0)
      this.trainingTranslationId = Number(this.$route.query.trainingTranslationId ?? 0)
      this.targetLanguageId = Number(this.$route.query.languageId ?? 0)
      this.state = this.resolveState()
      this.getLocations()
      this.getDropdowns()
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
    },

    loadNewTranslationState() {
      const targetLanguage = this.languages.find(
        (language) => language.languageId === this.targetLanguageId,
      )
      this.resetTranslation()
      this.translation.languageId = this.targetLanguageId
      this.translation.languageCode = targetLanguage ? targetLanguage.languageCode : ''
      this.getTraining()
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

    // Kategooriad ja rahastustüübid kasutajaliidese keeles (store'i contentLang)
    getDropdowns() {
      CategoryService.sendGetCategoriesRequest(this.contentLang)
        .then((response) => (this.categories = response.data))
        .catch(() => NavigationService.navigateToErrorView())
      FundingTypeService.sendGetFundingTypesRequest(this.contentLang)
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
      this.successMessage = this.$t('trainingForm.messages.trainingAdded', {
        title: this.translation.title,
      })
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
      this.successMessage = this.$t('trainingForm.messages.saved')
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
      this.successMessage = this.$t('trainingForm.messages.translationAdded', {
        language: this.translation.languageCode,
      })
      NavigationService.replaceTrainingFormView({
        trainingId: this.trainingId,
        trainingTranslationId: trainingTranslationCreateResponse.trainingTranslationId,
      })
    },

    // ---------- Lipukesed ----------

    // translationLanguage on GET /api/languages vastuse element, seega languageId on alati olemas
    handleTranslationFlagClicked(translationLanguage) {
      if (
        this.isNewTraining ||
        translationLanguage.languageCode === this.translation.languageCode
      ) {
        return
      }
      this.resetMessages()
      const existingTranslation = this.trainingTranslations.find(
        (trainingTranslation) =>
          trainingTranslation.languageCode === translationLanguage.languageCode,
      )

      if (existingTranslation) {
        NavigationService.replaceTrainingFormView({
          trainingId: this.trainingId,
          trainingTranslationId: existingTranslation.trainingTranslationId,
        })
      } else {
        NavigationService.replaceTrainingFormView({
          trainingId: this.trainingId,
          languageId: translationLanguage.languageId,
        })
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
        ? this.$t('trainingForm.messages.unpublished')
        : this.$t('trainingForm.messages.published')
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
      this.successMessage = this.$t('trainingForm.messages.aiDone')
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
        this.errorMessage = this.$t('trainingForm.validation.selectCategory')
      } else if (this.training.trainingLanguageId === 0) {
        this.errorMessage = this.$t('trainingForm.validation.selectTrainingLanguage')
      } else if (this.training.locationId === 0) {
        this.errorMessage = this.$t('trainingForm.validation.selectLocation')
      }
    },

    checkTranslationForErrors() {
      if (!this.errorMessageIsEmpty()) {
        return
      }
      if (this.translation.title.trim() === '') {
        this.errorMessage = this.$t('trainingForm.validation.addTitle')
      } else if (this.translation.shortDescription.trim() === '') {
        this.errorMessage = this.$t('trainingForm.validation.addShortDescription')
      } else if (this.translation.description.trim() === '') {
        this.errorMessage = this.$t('trainingForm.validation.addDescription')
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
            {{
              isPublished
                ? $t('trainingForm.status.published')
                : $t('trainingForm.status.unpublished')
            }}
          </span>
          <TranslationFlags
            v-if="!isNewTraining"
            class="ms-auto"
            :translation-languages="translationLanguages"
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
            {{ $t('trainingForm.buttons.add') }}
          </button>
          <button v-if="isUpdate" @click="updateTraining" class="btn btn-success" type="button">
            {{ $t('trainingForm.buttons.save') }}
          </button>
          <button
            v-if="isNewTranslation"
            @click="addTrainingTranslation"
            class="btn btn-success"
            type="button"
          >
            {{ $t('trainingForm.buttons.addTranslation') }}
          </button>
          <button
            v-if="!isNewTraining"
            @click="isStatusModalOpen = true"
            class="btn btn-outline-primary"
            type="button"
          >
            {{
              isPublished
                ? $t('trainingForm.buttons.unpublish')
                : $t('trainingForm.buttons.publish')
            }}
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
      :title="$t('trainingForm.aiModal.title')"
      :message="$t('trainingForm.aiModal.message')"
      :confirm-label="$t('trainingForm.aiModal.confirm')"
      @event-confirmed="getAiTranslation"
      @event-modal-closed="isAiConfirmModalOpen = false"
    />
  </div>
</template>
