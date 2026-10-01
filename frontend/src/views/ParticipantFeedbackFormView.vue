<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import UserService from '@/api-services/UserService.js'
import FormatService from '@/services/FormatService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import FeedbackCriteriaItem from '@/components/profile/FeedbackCriteriaItem.vue'
import ProfileMenu from '@/components/profile/ProfileMenu.vue'

// Pärast nende vigade teadet laaditakse vorm uuesti (tagasiside või kriteeriumid muutusid vahepeal)
const RELOAD_ERROR_CODES = [
  'FEEDBACK_ALREADY_EXISTS',
  'FEEDBACK_CRITERIA_CHANGED',
  'FEEDBACK_NOT_FOUND',
]

// Tagasiside: /participant-feedback-form?courseParticipantId={id} (router guard: sisse logimata → login, admin → NotAuthorizedView).
// hasFeedback = false → uus tagasiside (POST); true → lugemisrežiim, "Muuda" → "Salvesta" (PUT) / "Tühista".
// Otsused: docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-skeemid.md
export default {
  name: 'ParticipantFeedbackFormView',
  components: { InlineAlerts, FeedbackCriteriaItem, ProfileMenu },
  data() {
    return {
      courseParticipantId: 0,
      participantFeedback: null,
      // feedbackCriteriaId → { score, feedbackText }
      answers: {},
      isEditing: false,
      isSending: false,
      invalidFeedbackCriteriaIds: [],
      loadErrorMessage: '',
      successMessage: '',
      errorMessage: '',
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    isReadOnly() {
      return !this.isEditing
    },

    dateRangeText() {
      return FormatService.formatDateRange(
        this.participantFeedback.startDate,
        this.participantFeedback.endDate,
      )
    },

    submittedText() {
      return this.$t('participantFeedback.submitted', {
        date: FormatService.formatDateTime(this.participantFeedback.createdAt).split(' ')[0],
      })
    },

    // Kuvatakse ainult kuupäevad, seega "Muudetud" ainult siis, kui kuupäev erineb esitamise kuupäevast
    updatedText() {
      const createdDate = FormatService.formatDateTime(this.participantFeedback.createdAt).split(
        ' ',
      )[0]
      const updatedDate = FormatService.formatDateTime(this.participantFeedback.updatedAt).split(
        ' ',
      )[0]
      return updatedDate !== '' && updatedDate !== createdDate
        ? this.$t('participantFeedback.updated', { date: updatedDate })
        : ''
    },
  },
  watch: {
    contentLang() {
      this.getParticipantFeedback()
    },
  },
  methods: {
    getParticipantFeedback() {
      UserService.sendGetFeedbackRequest(
        SessionStorageService.getUserId(),
        this.courseParticipantId,
        this.contentLang,
      )
        .then((response) => this.handleGetParticipantFeedbackResponse(response.data))
        .catch((error) => this.handleGetParticipantFeedbackError(error))
    },

    // Keele vahetusel muutuvad ainult tekstid; muutmise ajal sisestatud väärtused jäävad alles
    handleGetParticipantFeedbackResponse(participantFeedback) {
      const isKeepingAnswers = this.isEditing && this.participantFeedback !== null
      this.participantFeedback = participantFeedback
      if (!isKeepingAnswers) {
        this.resetAnswers()
        this.isEditing = !participantFeedback.hasFeedback
      }
    },

    // 403 FEEDBACK_NOT_ALLOWED, 404 REGISTRATION_NOT_FOUND / PRIMARY_KEY_NOT_FOUND → teade, vormi pole
    handleGetParticipantFeedbackError(error) {
      const statusCode = error.response?.status
      if (statusCode === 403 || statusCode === 404) {
        this.participantFeedback = null
        this.loadErrorMessage = error.response.data?.message ?? ''
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    resetAnswers() {
      const answers = {}
      this.participantFeedback.criteria.forEach((criteria) => {
        answers[criteria.feedbackCriteriaId] = {
          score: criteria.score,
          feedbackText: criteria.feedbackText ?? '',
        }
      })
      this.answers = answers
      this.invalidFeedbackCriteriaIds = []
    },

    startEditing() {
      this.successMessage = ''
      this.errorMessage = ''
      this.isEditing = true
    },

    // Laaditud väärtused tagasi, lugemisrežiim
    cancelEditing() {
      this.errorMessage = ''
      this.resetAnswers()
      this.isEditing = false
    },

    changeScore(feedbackCriteriaId, score) {
      this.answers[feedbackCriteriaId].score = score
      this.invalidFeedbackCriteriaIds = this.invalidFeedbackCriteriaIds.filter(
        (invalidId) => invalidId !== feedbackCriteriaId,
      )
      if (
        this.invalidFeedbackCriteriaIds.length === 0 &&
        this.errorMessage === this.$t('participantFeedback.validation.scoreAll')
      ) {
        this.errorMessage = ''
      }
    },

    changeFeedbackText(feedbackCriteriaId, feedbackText) {
      this.answers[feedbackCriteriaId].feedbackText = feedbackText
    },

    saveFeedback() {
      this.successMessage = ''
      this.errorMessage = ''
      this.invalidFeedbackCriteriaIds = this.participantFeedback.criteria
        .map((criteria) => criteria.feedbackCriteriaId)
        .filter((feedbackCriteriaId) => this.answers[feedbackCriteriaId].score === null)
      if (this.invalidFeedbackCriteriaIds.length > 0) {
        this.errorMessage = this.$t('participantFeedback.validation.scoreAll')
        return
      }
      const feedbackRequest = {
        answers: this.participantFeedback.criteria.map((criteria) => {
          const answer = this.answers[criteria.feedbackCriteriaId]
          return {
            feedbackCriteriaId: criteria.feedbackCriteriaId,
            score: answer.score,
            feedbackText: answer.feedbackText.trim() === '' ? null : answer.feedbackText.trim(),
          }
        }),
      }
      const userId = SessionStorageService.getUserId()
      const request = this.participantFeedback.hasFeedback
        ? UserService.sendPutFeedbackRequest(userId, this.courseParticipantId, feedbackRequest)
        : UserService.sendPostFeedbackRequest(userId, this.courseParticipantId, feedbackRequest)
      this.isSending = true
      request
        .then(() => this.handleSaveFeedbackResponse())
        .catch((error) => this.handleSaveFeedbackError(error))
        .finally(() => (this.isSending = false))
    },

    handleSaveFeedbackResponse() {
      this.isEditing = false
      this.successMessage = this.$t('participantFeedback.messages.saved')
      this.getParticipantFeedback()
    },

    // 403/404 tagasiside vead → backendi teade ja vorm uuesti; 400 → backendi teade; muu → veavaade
    handleSaveFeedbackError(error) {
      const statusCode = error.response?.status
      const errorCode = error.response?.data?.errorCode
      if (RELOAD_ERROR_CODES.includes(errorCode)) {
        this.errorMessage = error.response.data.message
        this.isEditing = false
        this.getParticipantFeedback()
      } else if (statusCode === 400 || statusCode === 403 || statusCode === 404) {
        this.errorMessage = error.response.data?.message ?? ''
      } else {
        NavigationService.navigateToErrorView()
      }
    },
  },
  beforeMount() {
    this.courseParticipantId = Number(this.$route.query.courseParticipantId ?? 0)
    if (this.courseParticipantId === 0) {
      NavigationService.navigateToErrorView()
      return
    }
    this.getParticipantFeedback()
  },
}
</script>

<template>
  <div class="container">
    <div class="row g-4 text-start mb-5">
      <div class="col-lg-3">
        <ProfileMenu />
      </div>

      <div class="col-lg-9">
        <RouterLink :to="{ name: 'participantCoursesRoute' }" class="d-inline-block mb-3">
          ← {{ $t('participantFeedback.back') }}
        </RouterLink>

        <fieldset v-if="loadErrorMessage" class="border rounded bg-body p-3">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('participantFeedback.title') }}</legend>
          <div class="alert alert-danger mb-0" role="alert">{{ loadErrorMessage }}</div>
        </fieldset>

        <fieldset v-else-if="participantFeedback" class="border rounded bg-body p-3">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('participantFeedback.title') }}</legend>

          <div class="mb-3">
            <div class="fw-semibold">{{ participantFeedback.trainingTitle }}</div>
            <div class="small text-secondary">
              {{ dateRangeText }}
              <template v-if="participantFeedback.createdAt"> · {{ submittedText }}</template>
              <template v-if="updatedText"> · {{ updatedText }}</template>
            </div>
          </div>

          <p class="text-secondary">{{ $t('participantFeedback.scaleHint') }}</p>

          <div class="d-grid gap-3">
            <FeedbackCriteriaItem
              v-for="criteria in participantFeedback.criteria"
              :key="criteria.feedbackCriteriaId"
              :criteria="criteria"
              :answer="answers[criteria.feedbackCriteriaId]"
              :is-read-only="isReadOnly"
              :is-invalid="invalidFeedbackCriteriaIds.includes(criteria.feedbackCriteriaId)"
              @event-score-changed="(score) => changeScore(criteria.feedbackCriteriaId, score)"
              @event-feedback-text-changed="
                (feedbackText) => changeFeedbackText(criteria.feedbackCriteriaId, feedbackText)
              "
            />
          </div>

          <div class="d-flex flex-wrap align-items-center gap-2 mt-3">
            <button
              v-if="!participantFeedback.hasFeedback"
              @click="saveFeedback"
              :disabled="isSending"
              class="btn btn-primary"
              type="button"
            >
              {{ $t('participantFeedback.add') }}
            </button>
            <button
              v-else-if="!isEditing"
              @click="startEditing"
              class="btn btn-primary"
              type="button"
            >
              {{ $t('participantFeedback.edit') }}
            </button>
            <template v-else>
              <button
                @click="saveFeedback"
                :disabled="isSending"
                class="btn btn-success"
                type="button"
              >
                {{ $t('participantFeedback.save') }}
              </button>
              <button @click="cancelEditing" class="btn btn-outline-secondary" type="button">
                {{ $t('participantFeedback.cancel') }}
              </button>
            </template>
            <InlineAlerts
              :success-message="successMessage"
              :error-message="errorMessage"
              @event-success-message-closed="successMessage = ''"
              @event-error-message-closed="errorMessage = ''"
            />
          </div>
        </fieldset>
      </div>
    </div>
  </div>
</template>
