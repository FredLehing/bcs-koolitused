<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import UserService from '@/api-services/UserService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'
import ParticipantRegistrationItem from '@/components/profile/ParticipantRegistrationItem.vue'
import ProfileMenu from '@/components/profile/ProfileMenu.vue'

// Minu koolitused: /participant-courses (router guard: sisse logimata → login, admin → NotAuthorizedView).
// Backend annab registreerumised alguse järgi kasvavalt; "Toimunud" näidatakse uusimad eespool.
export default {
  name: 'ParticipantCoursesView',
  components: { InlineAlerts, ConfirmModal, ParticipantRegistrationItem, ProfileMenu },
  data() {
    return {
      registrations: null,
      registrationToCancel: null,
      isSending: false,
      successMessage: '',
      errorMessage: '',
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    upcomingRegistrations() {
      return this.registrations.filter((registration) => !registration.isPast)
    },

    pastRegistrations() {
      return this.registrations.filter((registration) => registration.isPast).reverse()
    },

    cancelModalMessage() {
      return this.registrationToCancel
        ? this.$t('participantCourses.cancelModal.message', {
            title: this.registrationToCancel.trainingTitle,
          })
        : ''
    },
  },
  watch: {
    contentLang() {
      this.getMyRegistrations()
    },
  },
  methods: {
    getMyRegistrations() {
      UserService.sendGetMyRegistrationsRequest(SessionStorageService.getUserId(), this.contentLang)
        .then((response) => (this.registrations = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // ---------- "Loobu" ----------

    openCancelModal(registration) {
      this.successMessage = ''
      this.errorMessage = ''
      this.registrationToCancel = registration
    },

    cancelRegistration() {
      const courseParticipantId = this.registrationToCancel.courseParticipantId
      this.registrationToCancel = null
      this.isSending = true
      UserService.sendPutCancelRegistrationRequest(
        SessionStorageService.getUserId(),
        courseParticipantId,
      )
        .then(() => this.handleCancelRegistrationResponse())
        .catch((error) => this.handleCancelRegistrationError(error))
        .finally(() => (this.isSending = false))
    },

    handleCancelRegistrationResponse() {
      this.successMessage = this.$t('participantCourses.messages.cancelled')
      this.getMyRegistrations()
    },

    // 403 CANCEL_NOT_ALLOWED, 404 REGISTRATION_NOT_FOUND → backendi teade; nimekiri laaditakse uuesti
    handleCancelRegistrationError(error) {
      const statusCode = error.response?.status
      if (statusCode === 403 || statusCode === 404) {
        this.errorMessage = error.response.data?.message ?? ''
        this.getMyRegistrations()
      } else {
        NavigationService.navigateToErrorView()
      }
    },
  },
  beforeMount() {
    this.getMyRegistrations()
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
        <fieldset v-if="registrations" class="border rounded bg-body p-3">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('navbar.participantCourses') }}
          </legend>

          <InlineAlerts
            :success-message="successMessage"
            :error-message="errorMessage"
            class="mb-3"
            @event-success-message-closed="successMessage = ''"
            @event-error-message-closed="errorMessage = ''"
          />

          <div v-if="registrations.length === 0">
            <p>{{ $t('participantCourses.empty') }}</p>
            <RouterLink :to="{ name: 'coursesRoute' }">
              {{ $t('participantCourses.browseCourses') }}
            </RouterLink>
          </div>

          <template v-else>
            <h2 class="h6 text-uppercase text-secondary">
              {{ $t('participantCourses.upcoming') }}
            </h2>
            <p v-if="upcomingRegistrations.length === 0" class="text-secondary">
              {{ $t('participantCourses.noUpcoming') }}
            </p>
            <ul v-else class="list-group mb-4">
              <ParticipantRegistrationItem
                v-for="registration in upcomingRegistrations"
                :key="registration.courseParticipantId"
                :registration="registration"
                :is-sending="isSending"
                @event-cancel-clicked="openCancelModal"
              />
            </ul>

            <template v-if="pastRegistrations.length > 0">
              <h2 class="h6 text-uppercase text-secondary">
                {{ $t('participantCourses.past') }}
              </h2>
              <ul class="list-group">
                <ParticipantRegistrationItem
                  v-for="registration in pastRegistrations"
                  :key="registration.courseParticipantId"
                  :registration="registration"
                />
              </ul>
            </template>
          </template>
        </fieldset>
      </div>
    </div>

    <ConfirmModal
      :is-open="registrationToCancel !== null"
      :title="$t('participantCourses.cancelModal.title')"
      :message="cancelModalMessage"
      :confirm-label="$t('participantCourses.cancelModal.confirm')"
      @event-confirmed="cancelRegistration"
      @event-modal-closed="registrationToCancel = null"
    />
  </div>
</template>
