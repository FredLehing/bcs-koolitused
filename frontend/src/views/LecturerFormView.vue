<script>
import LecturerService from '@/api-services/LecturerService.js'
import LecturerTranslationService from '@/api-services/LecturerTranslationService.js'
import LanguageService from '@/api-services/LanguageService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import TranslationFlags from '@/components/common/TranslationFlags.vue'
import PhotoUpload from '@/components/forms/PhotoUpload.vue'
import LecturerTranslationForm from '@/components/forms/LecturerTranslationForm.vue'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Vaate olekud (state) tuletatakse URL-i query parameetritest (sama muster nagu TrainingFormView):
//   new-lecturer     /lecturer-form
//   update           /lecturer-form?lecturerId={id}&lecturerTranslationId={id}
//   new-translation  /lecturer-form?lecturerId={id}&languageId={id}
const STATE_NEW_LECTURER = 'new-lecturer'
const STATE_UPDATE = 'update'
const STATE_NEW_TRANSLATION = 'new-translation'

// Backendi vead, mis kuvatakse vormis (muu → üldine veavaade)
const FORM_ERROR_STATUSES = [400, 403]

function htmlHasText(html) {
  return new DOMParser().parseFromString(html, 'text/html').body.textContent.trim() !== ''
}

export default {
  name: 'LecturerFormView',
  components: {
    ConfirmModal,
    LecturerTranslationForm,
    PhotoUpload,
    TranslationFlags,
    InlineAlerts,
  },
  data() {
    return {
      state: STATE_NEW_LECTURER,
      successMessage: '',
      errorMessage: '',

      lecturerId: 0,
      lecturerTranslationId: 0,
      targetLanguageId: 0,

      languages: [],
      lecturerTranslations: [],

      lecturer: {
        lecturerId: 0,
        fullName: '',
        photoVersion: null,
      },

      // Uus valitud pilt { photo, photoContentType } — saadetakse ainult siis (salvestatud pilti tagasi ei saadeta)
      newPhoto: null,
      isPhotoRemoved: false,

      translation: {
        lecturerTranslationId: 0,
        languageId: 0,
        languageCode: '',
        title: '',
        shortDescription: '',
        description: '',
      },

      // Viimati laaditud/salvestatud tõlketekstid — salvestamata muudatuste tuvastamiseks
      savedTranslationTexts: '',

      isAiConfirmModalOpen: false,
      isAiLoading: false,
      isSending: false,
    }
  },
  computed: {
    // Keeled, millesse koolitaja tekstid tõlgitakse (lipukesed)
    translationLanguages() {
      return this.languages.filter((language) => language.requiresTranslation)
    },

    mainLanguageCode() {
      const mainLanguage = this.languages.find((language) => language.isMainLanguage)
      return mainLanguage ? mainLanguage.languageCode : ''
    },

    isNewLecturer() {
      return this.state === STATE_NEW_LECTURER
    },

    isUpdate() {
      return this.state === STATE_UPDATE
    },

    isNewTranslation() {
      return this.state === STATE_NEW_TRANSLATION
    },

    pageTitle() {
      if (this.isNewLecturer) {
        return this.$t('lecturerForm.title.newLecturer')
      } else if (this.isNewTranslation) {
        return this.$t('lecturerForm.title.newTranslation')
      }
      return this.$t('lecturerForm.title.update')
    },

    translationLanguage() {
      return this.languages.find(
        (language) => language.languageCode === this.translation.languageCode,
      )
    },

    translationLanguageName() {
      return this.translationLanguage
        ? this.translationLanguage.languageName
        : this.translation.languageCode
    },

    showAiButton() {
      return (
        this.isNewTranslation ||
        (this.isUpdate && this.translation.languageCode !== this.mainLanguageCode)
      )
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
      this.lecturerId = Number(this.$route.query.lecturerId ?? 0)
      this.lecturerTranslationId = Number(this.$route.query.lecturerTranslationId ?? 0)
      this.targetLanguageId = Number(this.$route.query.languageId ?? 0)
      this.state = this.resolveState()
      this.newPhoto = null
      this.isPhotoRemoved = false
      this.getLanguages()
    },

    resolveState() {
      if (this.lecturerId === 0) {
        return STATE_NEW_LECTURER
      } else if (this.lecturerTranslationId !== 0) {
        return STATE_UPDATE
      }
      return STATE_NEW_TRANSLATION
    },

    getLanguages() {
      LanguageService.sendGetLanguagesRequest()
        .then((response) => this.handleGetLanguagesResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Oleku andmed laaditakse pärast keelte nimekirja, sest languageId ↔ languageCode teisendus vajab seda
    handleGetLanguagesResponse(languages) {
      this.languages = languages
      if (this.isNewLecturer) {
        this.loadNewLecturerState()
      } else if (this.isUpdate) {
        this.loadUpdateState()
      } else {
        this.loadNewTranslationState()
      }
    },

    loadNewLecturerState() {
      this.resetLecturer()
      this.resetTranslation()
      this.lecturerTranslations = []
      this.translation.languageCode = this.mainLanguageCode
      this.savedTranslationTexts = this.getTranslationTexts()
    },

    loadUpdateState() {
      this.getLecturer()
      this.getLecturerTranslations()
      LecturerTranslationService.sendGetLecturerTranslationRequest(this.lecturerTranslationId)
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
      this.getLecturer()
      LecturerService.sendGetLecturerTranslationsRequest(this.lecturerId)
        .then((response) => this.handleGetNewTranslationLecturerTranslationsResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetNewTranslationLecturerTranslationsResponse(lecturerTranslations) {
      this.lecturerTranslations = lecturerTranslations
      const mainTranslation = lecturerTranslations.find(
        (lecturerTranslation) => lecturerTranslation.isMainLanguage,
      )
      LecturerTranslationService.sendGetLecturerTranslationRequest(
        mainTranslation.lecturerTranslationId,
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

    getLecturer() {
      LecturerService.sendGetLecturerRequest(this.lecturerId)
        .then((response) => (this.lecturer = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getLecturerTranslations() {
      LecturerService.sendGetLecturerTranslationsRequest(this.lecturerId)
        .then((response) => (this.lecturerTranslations = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // ---------- "Lisa" (new-lecturer) ----------

    addLecturer() {
      this.resetMessages()
      this.checkFormForErrors()
      if (this.errorMessageIsEmpty()) {
        this.isSending = true
        LecturerService.sendPostLecturerRequest({
          userId: SessionStorageService.getUserId(),
          fullName: this.lecturer.fullName,
          photo: this.newPhoto ? this.newPhoto.photo : null,
          photoContentType: this.newPhoto ? this.newPhoto.photoContentType : null,
          title: this.translation.title,
          shortDescription: this.translation.shortDescription,
          description: this.translation.description,
        })
          .then((response) => this.handleAddLecturerResponse(response.data))
          .catch((error) => this.handleSaveError(error))
          .finally(() => (this.isSending = false))
      }
    },

    handleAddLecturerResponse(lecturerCreateResponse) {
      this.successMessage = this.$t('lecturerForm.messages.lecturerAdded')
      NavigationService.replaceLecturerFormView({
        lecturerId: lecturerCreateResponse.lecturerId,
        lecturerTranslationId: lecturerCreateResponse.lecturerTranslationId,
      })
    },

    // ---------- "Salvesta" (update) ----------

    updateLecturer() {
      this.resetMessages()
      this.checkFormForErrors()
      if (this.errorMessageIsEmpty()) {
        this.isSending = true
        LecturerService.sendPutLecturerRequest(this.lecturerId, this.createLecturerUpdateRequest())
          .then(() => this.handleUpdateLecturerResponse())
          .catch((error) => this.handleSaveError(error))
          .finally(() => (this.isSending = false))
      }
    },

    createLecturerUpdateRequest() {
      return {
        fullName: this.lecturer.fullName,
        photo: this.newPhoto ? this.newPhoto.photo : null,
        photoContentType: this.newPhoto ? this.newPhoto.photoContentType : null,
        isPhotoRemoved: this.isPhotoRemoved,
        lecturerTranslation: {
          lecturerTranslationId: this.translation.lecturerTranslationId,
          title: this.translation.title,
          shortDescription: this.translation.shortDescription,
          description: this.translation.description,
        },
      }
    },

    // Pildi versioon muutus → laadi koolitaja uuesti (eelvaade pilditeenusest)
    handleUpdateLecturerResponse() {
      this.successMessage = this.$t('lecturerForm.messages.saved')
      this.savedTranslationTexts = this.getTranslationTexts()
      this.newPhoto = null
      this.isPhotoRemoved = false
      this.getLecturer()
    },

    // ---------- "Lisa tõlge" (new-translation) ----------

    addLecturerTranslation() {
      this.resetMessages()
      this.checkTranslationForErrors()
      if (this.errorMessageIsEmpty()) {
        this.isSending = true
        LecturerService.sendPostLecturerTranslationRequest(this.lecturerId, {
          languageId: this.targetLanguageId,
          title: this.translation.title,
          shortDescription: this.translation.shortDescription,
          description: this.translation.description,
        })
          .then((response) => this.handleAddLecturerTranslationResponse(response.data))
          .catch((error) => this.handleSaveError(error))
          .finally(() => (this.isSending = false))
      }
    },

    handleAddLecturerTranslationResponse(lecturerTranslationCreateResponse) {
      this.successMessage = this.$t('lecturerForm.messages.translationAdded')
      NavigationService.replaceLecturerFormView({
        lecturerId: this.lecturerId,
        lecturerTranslationId: lecturerTranslationCreateResponse.lecturerTranslationId,
      })
    },

    // 400 ja 403 (pildi vead, TRANSLATION_EXISTS) → backendi teade vormis; 404 ja 500 → veavaade
    handleSaveError(error) {
      if (FORM_ERROR_STATUSES.includes(error.response?.status)) {
        this.errorMessage = error.response.data.message
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    // ---------- Lipukesed ----------

    handleTranslationFlagClicked(translationLanguage) {
      if (
        this.isNewLecturer ||
        translationLanguage.languageCode === this.translation.languageCode
      ) {
        return
      }
      this.resetMessages()
      const existingTranslation = this.lecturerTranslations.find(
        (lecturerTranslation) =>
          lecturerTranslation.languageCode === translationLanguage.languageCode,
      )
      if (existingTranslation) {
        NavigationService.replaceLecturerFormView({
          lecturerId: this.lecturerId,
          lecturerTranslationId: existingTranslation.lecturerTranslationId,
        })
      } else {
        NavigationService.replaceLecturerFormView({
          lecturerId: this.lecturerId,
          languageId: translationLanguage.languageId,
        })
      }
    },

    // ---------- Pilt ----------

    handlePhotoSelected(newPhoto) {
      this.resetMessages()
      this.newPhoto = newPhoto
      this.isPhotoRemoved = false
    },

    // Salvestatud pilt eemaldatakse "Salvesta" järel; uus (salvestamata) pilt lihtsalt unustatakse
    handlePhotoRemoved() {
      this.newPhoto = null
      this.isPhotoRemoved = this.lecturer.photoVersion !== null
    },

    handlePhotoError(message) {
      this.resetMessages()
      this.errorMessage = message
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
      LecturerService.sendGetAiTranslationRequest(this.lecturerId, this.translation.languageId)
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
      const errorCode = error.response?.data?.errorCode
      if (
        (statusCode === 503 && errorCode === 'AI_SERVICE_UNAVAILABLE') ||
        (statusCode === 403 && errorCode === 'MAIN_LANGUAGE_NOT_TRANSLATABLE')
      ) {
        this.errorMessage = error.response.data.message
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

    // ---------- Valideerimine ----------

    checkFormForErrors() {
      if (this.lecturer.fullName.trim() === '') {
        this.errorMessage = this.$t('lecturerForm.validation.fillRequired')
        return
      }
      this.checkTranslationForErrors()
    },

    checkTranslationForErrors() {
      if (
        this.translation.title.trim() === '' ||
        this.translation.shortDescription.trim() === '' ||
        !htmlHasText(this.translation.description)
      ) {
        this.errorMessage = this.$t('lecturerForm.validation.fillRequired')
      }
    },

    errorMessageIsEmpty() {
      return this.errorMessage === ''
    },

    resetLecturer() {
      this.lecturer = {
        lecturerId: 0,
        fullName: '',
        photoVersion: null,
      }
    },

    resetTranslation() {
      this.translation = {
        lecturerTranslationId: 0,
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

    navigateToAdminLecturersView() {
      NavigationService.navigateToAdminLecturersView()
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
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
          <h1 class="mb-0">
            {{ pageTitle }}
            <span v-if="!isNewLecturer && lecturer.fullName" class="text-secondary fs-4">
              — {{ lecturer.fullName }}
            </span>
          </h1>
          <button
            @click="navigateToAdminLecturersView"
            class="btn btn-outline-secondary"
            type="button"
          >
            {{ $t('navbar.manageLecturers') }}
          </button>
        </div>

        <fieldset v-if="!isNewLecturer" class="border rounded p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('lecturerForm.translations.legend') }}
          </legend>
          <TranslationFlags
            :translation-languages="translationLanguages"
            :existing-translations="lecturerTranslations"
            :current-language-code="translation.languageCode"
            @event-translation-flag-clicked="handleTranslationFlagClicked"
          />
        </fieldset>

        <fieldset class="border rounded p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('lecturerForm.data.legend') }}</legend>
          <div class="row g-3 text-start">
            <div class="col-md-6">
              <label class="form-label" for="fullName"
                >{{ $t('lecturerForm.data.fullName') }} *</label
              >
              <input
                v-model="lecturer.fullName"
                :readonly="isNewTranslation"
                id="fullName"
                class="form-control"
                type="text"
                maxlength="255"
              />
            </div>
            <div class="col-md-6">
              <label class="form-label">{{ $t('lecturerForm.data.photo') }}</label>
              <PhotoUpload
                :lecturer-id="lecturer.lecturerId"
                :photo-version="lecturer.photoVersion"
                :new-photo="newPhoto"
                :is-photo-removed="isPhotoRemoved"
                :is-readonly="isNewTranslation"
                @event-photo-selected="handlePhotoSelected"
                @event-photo-removed="handlePhotoRemoved"
                @event-photo-error="handlePhotoError"
              />
            </div>
          </div>
        </fieldset>

        <LecturerTranslationForm
          :translation="translation"
          :language-name="translationLanguageName"
          :flag-icon-code="translationLanguage ? translationLanguage.flagIconCode : ''"
          :show-ai-button="showAiButton"
          :is-ai-loading="isAiLoading"
          :show-prefilled-hint="isNewTranslation"
          :main-language-code="mainLanguageCode"
          @event-new-title-input="translation.title = $event"
          @event-new-short-description-input="translation.shortDescription = $event"
          @event-new-description-input="translation.description = $event"
          @event-ai-translation-clicked="handleAiTranslationClicked"
        />

        <div class="d-flex flex-wrap align-items-center gap-3 mb-5">
          <button
            v-if="isNewLecturer"
            @click="addLecturer"
            :disabled="isSending"
            class="btn btn-success"
            type="button"
          >
            {{ $t('trainingForm.buttons.add') }}
          </button>
          <button
            v-if="isUpdate"
            @click="updateLecturer"
            :disabled="isSending"
            class="btn btn-success"
            type="button"
          >
            {{ $t('trainingForm.buttons.save') }}
          </button>
          <button
            v-if="isNewTranslation"
            @click="addLecturerTranslation"
            :disabled="isSending"
            class="btn btn-success"
            type="button"
          >
            {{ $t('trainingForm.buttons.addTranslation') }}
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
      :is-open="isAiConfirmModalOpen"
      :title="$t('trainingForm.aiModal.title')"
      :message="$t('lecturerForm.aiModal.message')"
      :confirm-label="$t('trainingForm.aiModal.confirm')"
      @event-confirmed="getAiTranslation"
      @event-modal-closed="isAiConfirmModalOpen = false"
    />
  </div>
</template>
