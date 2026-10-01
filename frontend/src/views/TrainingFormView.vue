<script>
import BackLink from '@/components/common/BackLink.vue'
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import AiTrainingService from '@/api-services/AiTrainingService.js'
import TrainingTranslationService from '@/api-services/TrainingTranslationService.js'
import LanguageService from '@/api-services/LanguageService.js'
import CategoryService from '@/api-services/CategoryService.js'
import FundingTypeService from '@/api-services/FundingTypeService.js'
import LocationService from '@/api-services/LocationService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import TranslationFlags from '@/components/common/TranslationFlags.vue'
import TrainingDataForm from '@/components/forms/TrainingDataForm.vue'
import TrainingTranslationForm from '@/components/forms/TrainingTranslationForm.vue'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'
import TrainingStatusButton from '@/components/common/TrainingStatusButton.vue'

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
    BackLink,
    ConfirmModal,
    TrainingStatusButton,
    TrainingTranslationForm,
    TrainingDataForm,
    TranslationFlags,
    InlineAlerts,
  },
  data() {
    return {
      state: STATE_NEW_TRAINING,
      newCurriculum: null,
      isCurriculumRemoved: false,
      isCurriculumLoading: false,
      isSaving: false,
      successMessage: '',
      errorMessage: '',

      trainingId: 0,
      trainingTranslationId: 0,
      targetLanguageId: 0,

      languages: [],
      categories: [],
      fundingTypes: [],
      locations: [],
      trainingTranslations: [],

      training: {
        trainingId: 0,
        categoryId: 0,
        trainingLanguageId: 0,
        locationId: 0,
        lecturers: [],
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
        curriculumFileName: null,
        curriculumFileSize: null,
      },

      // Viimati laaditud/salvestatud tõlketekstid — salvestamata muudatuste tuvastamiseks
      savedTranslationTexts: '',

      errorResponse: {
        message: '',
        errorCode: '',
      },

      isAiConfirmModalOpen: false,
      isAiLoading: false,
      isAiPdfConfirmModalOpen: false,
      isAiPdfLoading: false,
      aiRequestId: 0,
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

    isAiBusy() {
      return this.isAiLoading || this.isAiPdfLoading
    },

    isAiActionDisabled() {
      return this.isAiBusy || this.isSaving || this.isCurriculumLoading
    },

    showAiPdfButton() {
      return (
        !this.isCurriculumRemoved &&
        (!!this.newCurriculum?.file || (this.isUpdate && !!this.translation.curriculumFileName))
      )
    },

    aiPdfTooltip() {
      const tooltipKey = this.isNewTraining
        ? 'aiPdfTooltipNewTraining'
        : this.isNewTranslation
          ? 'aiPdfTooltipNewTranslation'
          : 'aiPdfTooltipUpdate'
      return this.$t(`trainingForm.translation.${tooltipKey}`)
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
      // Vana vaate AI vastus ei tohi täita äsja avatud vormi.
      this.aiRequestId++
      this.isAiLoading = false
      this.isAiPdfLoading = false
      this.isAiConfirmModalOpen = false
      this.isAiPdfConfirmModalOpen = false
      this.resetCurriculum()
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
      this.resetCurriculum()
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
      if (this.isAiActionDisabled) return
      this.resetMessages()
      this.checkTrainingDataForErrors()
      this.checkTranslationForErrors()

      if (this.errorMessageIsEmpty()) {
        this.isSaving = true
        TrainingService.sendPostTrainingRequest(this.createTrainingCreateRequest())
          .then((response) => this.handleAddTrainingResponse(response.data))
          .catch((error) => this.handleSaveTrainingError(error))
          .finally(() => (this.isSaving = false))
      }
    },

    createTrainingCreateRequest() {
      return {
        userId: SessionStorageService.getUserId(),
        categoryId: this.training.categoryId,
        trainingLanguageId: this.training.trainingLanguageId,
        locationId: this.training.locationId,
        lecturerIds: this.getLecturerIds(),
        isOrderable: this.training.isOrderable,
        isPromoted: this.training.isPromoted,
        fundingTypeIds: this.training.fundingTypeIds,
        title: this.translation.title,
        shortDescription: this.translation.shortDescription,
        description: this.translation.description,
        curriculum: this.newCurriculum?.curriculum ?? null,
        curriculumLabel: this.getCurriculumLabel(),
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
      if (this.isAiActionDisabled) return
      this.resetMessages()
      this.checkTrainingDataForErrors()
      this.checkTranslationForErrors()

      if (this.errorMessageIsEmpty()) {
        this.isSaving = true
        TrainingService.sendPutTrainingRequest(this.trainingId, this.createTrainingUpdateRequest())
          .then(() => this.handleUpdateTrainingResponse())
          .catch((error) => this.handleSaveTrainingError(error))
          .finally(() => (this.isSaving = false))
      }
    },

    createTrainingUpdateRequest() {
      return {
        categoryId: this.training.categoryId,
        trainingLanguageId: this.training.trainingLanguageId,
        locationId: this.training.locationId,
        lecturerIds: this.getLecturerIds(),
        isOrderable: this.training.isOrderable,
        isPromoted: this.training.isPromoted,
        fundingTypeIds: this.training.fundingTypeIds,
        trainingTranslationId: this.translation.trainingTranslationId,
        isCurriculumRemoved: this.isCurriculumRemoved,
        title: this.translation.title,
        shortDescription: this.translation.shortDescription,
        description: this.translation.description,
        curriculum: this.newCurriculum?.curriculum ?? null,
        curriculumLabel: this.getCurriculumLabel(),
      }
    },

    handleUpdateTrainingResponse() {
      return TrainingTranslationService.sendGetTrainingTranslationRequest(
        this.trainingTranslationId,
      ).then((response) => {
        this.handleGetUpdateTranslationResponse(response.data)
        this.successMessage = this.$t('trainingForm.messages.saved')
      })
    },

    getCurriculumLabel() {
      return this.$t(
        'trainingForm.translation.curriculum',
        {},
        { locale: this.translation.languageCode },
      )
    },

    resetCurriculum() {
      this.newCurriculum = null
      this.isCurriculumRemoved = false
      this.isCurriculumLoading = false
    },

    handleCurriculumSelected(curriculum) {
      this.newCurriculum = curriculum
      this.isCurriculumRemoved = false
    },

    handleCurriculumRemoved() {
      this.newCurriculum = null
      this.isCurriculumRemoved = true
    },

    // Vahepeal kustutatud koolitaja (404 'lecturerId') → backendi teade vormis, muu viga → veavaade
    handleSaveTrainingError(error) {
      this.errorResponse = error.response?.data ?? { message: '', errorCode: '' }
      if (
        error.response?.status === 403 &&
        ['CURRICULUM_TYPE_NOT_ALLOWED', 'CURRICULUM_TOO_LARGE'].includes(
          this.errorResponse.errorCode,
        )
      ) {
        this.errorMessage = this.errorResponse.message
      } else if (
        error.response?.status === 404 &&
        this.errorResponse.message.includes("'lecturerId'")
      ) {
        this.errorMessage = this.errorResponse.message
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    // ---------- "Lisa tõlge" (new-translation) ----------

    addTrainingTranslation() {
      if (this.isAiActionDisabled) return
      this.resetMessages()
      this.checkTranslationForErrors()

      if (this.errorMessageIsEmpty()) {
        this.isSaving = true
        TrainingService.sendPostTrainingTranslationRequest(this.trainingId, {
          languageId: this.targetLanguageId,
          title: this.translation.title,
          shortDescription: this.translation.shortDescription,
          description: this.translation.description,
          curriculum: this.newCurriculum?.curriculum ?? null,
          curriculumLabel: this.getCurriculumLabel(),
        })
          .then((response) => this.handleAddTrainingTranslationResponse(response.data))
          .catch((error) => this.handleSaveTrainingError(error))
          .finally(() => (this.isSaving = false))
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
        this.isAiActionDisabled ||
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

    // API kutse teeb TrainingStatusButton ise; vaade näitab teadet ja laadib koolituse uuesti
    handleChangeTrainingStatusResponse(newStatus) {
      this.resetMessages()
      this.successMessage =
        newStatus === 'P'
          ? this.$t('trainingForm.messages.published')
          : this.$t('trainingForm.messages.unpublished')
      this.getTraining()
    },

    // 403 TRAINING_DELETED — koolitus kustutati vahepeal; uuesti laadimine suunab 404 korral veavaatele
    handleChangeTrainingStatusError(message) {
      this.resetMessages()
      this.errorMessage = message
      this.getTraining()
    },

    // ---------- AI tõlge ja PDF-ist vormi täitmine ----------

    handleAiTranslationClicked() {
      if (this.isAiActionDisabled || !this.showAiButton) return
      if (this.translationHasUnsavedChanges()) {
        this.isAiConfirmModalOpen = true
      } else {
        this.getAiTranslation()
      }
    },

    getAiTranslation() {
      this.isAiConfirmModalOpen = false
      if (this.isAiActionDisabled || !this.showAiButton) return
      this.resetMessages()
      this.isAiLoading = true
      const aiRequestId = ++this.aiRequestId
      return this.handleAiTrainingRequest(
        AiTrainingService.sendPostTranslationRequest(this.trainingId, this.translation.languageId),
        aiRequestId,
        'aiDone',
      )
    },

    handleAiPdfClicked() {
      if (this.isAiActionDisabled || !this.showAiPdfButton) return
      if (this.translationHasUnsavedChanges()) {
        this.isAiPdfConfirmModalOpen = true
      } else {
        this.fillTrainingContentFromPdf()
      }
    },

    fillTrainingContentFromPdf() {
      this.isAiPdfConfirmModalOpen = false
      if (this.isAiActionDisabled || !this.showAiPdfButton) return
      this.resetMessages()
      this.isAiPdfLoading = true
      const aiRequestId = ++this.aiRequestId
      const curriculumFile = this.newCurriculum?.file
      const aiTrainingRequest = this.isUpdate
        ? AiTrainingService.sendPostTranslationPdfRequest(
            this.trainingTranslationId,
            curriculumFile,
          )
        : AiTrainingService.sendPostPdfRequest(curriculumFile)
      return this.handleAiTrainingRequest(aiTrainingRequest, aiRequestId, 'aiPdfDone')
    },

    handleAiTrainingRequest(aiTrainingRequest, aiRequestId, messageKey) {
      return aiTrainingRequest
        .then((response) => {
          if (aiRequestId === this.aiRequestId) {
            this.handleAiTrainingContentResponse(response.data, messageKey)
          }
        })
        .catch((error) => {
          if (aiRequestId === this.aiRequestId) this.handleAiTrainingError(error)
        })
        .finally(() => {
          if (aiRequestId === this.aiRequestId) {
            this.isAiLoading = false
            this.isAiPdfLoading = false
          }
        })
    },

    // AI tulemus kuvatakse ainult vormis; salvestamata muutuste võrdlusalus jääb alles.
    handleAiTrainingContentResponse(aiTrainingContent, messageKey) {
      const textFields = ['title', 'shortDescription', 'description']
      if (!textFields.every((field) => typeof aiTrainingContent?.[field] === 'string')) {
        throw new Error('AI vastuses puuduvad koolituse tekstiväljad')
      }
      this.translation.title = aiTrainingContent.title
      this.translation.shortDescription = aiTrainingContent.shortDescription
      this.translation.description = aiTrainingContent.description
      this.successMessage = this.$t(`trainingForm.messages.${messageKey}`)
    },

    handleAiTrainingError(error) {
      this.errorResponse = error.response?.data ?? { message: '', errorCode: '' }
      this.errorMessage =
        this.errorResponse.message ||
        this.errorResponse.detail ||
        this.$t('trainingForm.messages.aiFailed')
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

    // Koolitajate järjekord = sort_order
    getLecturerIds() {
      return this.training.lecturers.map((lecturer) => lecturer.lecturerId)
    },

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
        lecturers: [],
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
        curriculumFileName: null,
        curriculumFileSize: null,
      }
    },

    // Olekus "update" avatakse vormis avatud tõlge; "new-translation" olekus tõlget veel pole
    // (trainingTranslationId = 0) → vaade valib tõlke kasutajaliidese keele järgi
    navigateToTrainingView() {
      NavigationService.navigateToTrainingView(this.trainingId, this.trainingTranslationId)
    },

    navigateToAdminTrainingsView() {
      NavigationService.navigateToAdminTrainingsView()
    },

    navigateToAdminTrainingCoursesView() {
      NavigationService.navigateToAdminTrainingCoursesView(this.trainingId)
    },

    resetMessages() {
      this.successMessage = ''
      this.errorMessage = ''
    },
  },
  beforeUnmount() {
    this.aiRequestId++
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
    <BackLink :fallback="{ name: 'adminTrainingsRoute' }" />
    <div class="row justify-content-center">
      <div class="col-lg-10">
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
        </div>

        <fieldset v-if="!isNewTraining" class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('trainingForm.translations.legend') }}
          </legend>
          <TranslationFlags
            :translation-languages="translationLanguages"
            :existing-translations="trainingTranslations"
            :current-language-code="translation.languageCode"
            @event-translation-flag-clicked="handleTranslationFlagClicked"
          />
        </fieldset>

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
          @event-lecturers-changed="training.lecturers = $event"
          @event-funding-type-checkbox-updated="updateFundingTypeIds"
          @event-is-orderable-changed="training.isOrderable = $event"
          @event-is-promoted-changed="training.isPromoted = $event"
        />

        <TrainingTranslationForm
          :key="$route.fullPath"
          :translation="translation"
          :new-curriculum="newCurriculum"
          :is-curriculum-removed="isCurriculumRemoved"
          :is-saving="isSaving"
          :is-disabled="isAiActionDisabled"
          :show-ai-pdf-button="showAiPdfButton"
          :is-ai-pdf-loading="isAiPdfLoading"
          :ai-pdf-tooltip="aiPdfTooltip"
          @event-ai-pdf-clicked="handleAiPdfClicked"
          @event-curriculum-selected="handleCurriculumSelected"
          @event-curriculum-removed="handleCurriculumRemoved"
          @event-curriculum-loading="isCurriculumLoading = $event"
          :language-name="translationLanguageName"
          :show-ai-button="showAiButton"
          :is-ai-loading="isAiLoading"
          :ai-tooltip="aiTooltip"
          @event-new-title-input="translation.title = $event"
          @event-new-short-description-input="translation.shortDescription = $event"
          @event-new-description-input="translation.description = $event"
          @event-ai-translation-clicked="handleAiTranslationClicked"
        />

        <div class="d-flex flex-wrap align-items-center gap-3 mb-5">
          <button
            v-if="isNewTraining"
            :disabled="isAiActionDisabled"
            @click="addTraining"
            class="btn btn-success"
            type="button"
          >
            {{ $t('trainingForm.buttons.add') }}
          </button>
          <button
            v-if="isUpdate"
            :disabled="isAiActionDisabled"
            @click="updateTraining"
            class="btn btn-success"
            type="button"
          >
            {{ $t('trainingForm.buttons.save') }}
          </button>
          <button
            v-if="isNewTranslation"
            :disabled="isAiActionDisabled"
            @click="addTrainingTranslation"
            class="btn btn-success"
            type="button"
          >
            {{ $t('trainingForm.buttons.addTranslation') }}
          </button>
          <TrainingStatusButton
            v-if="!isNewTraining && training.status !== ''"
            :training-id="trainingId"
            :status="training.status"
            @event-status-changed="handleChangeTrainingStatusResponse"
            @event-status-error="handleChangeTrainingStatusError"
          />
          <button
            v-if="!isNewTraining"
            @click="navigateToTrainingView"
            class="btn btn-outline-secondary"
            type="button"
          >
            {{ $t('trainingForm.buttons.view') }}
          </button>
          <button
            v-if="!isNewTraining"
            @click="navigateToAdminTrainingCoursesView"
            class="btn btn-outline-secondary"
            type="button"
          >
            {{ $t('trainingForm.buttons.calendar') }}
          </button>
          <button
            @click="navigateToAdminTrainingsView"
            class="btn btn-outline-secondary"
            type="button"
          >
            {{ $t('navbar.manageTrainings') }}
          </button>
          <InlineAlerts
            :success-message="successMessage"
            :error-message="errorMessage"
            @event-success-message-closed="successMessage = ''"
            @event-error-message-closed="errorMessage = ''"
          />
        </div>
      </div>
    </div>

    <ConfirmModal
      :is-open="isAiPdfConfirmModalOpen"
      :title="$t('trainingForm.aiPdfModal.title')"
      :message="$t('trainingForm.aiPdfModal.message')"
      :confirm-label="$t('trainingForm.aiModal.confirm')"
      @event-confirmed="fillTrainingContentFromPdf"
      @event-modal-closed="isAiPdfConfirmModalOpen = false"
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
