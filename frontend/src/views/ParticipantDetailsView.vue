<script>
import UserService from '@/api-services/UserService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import ProfileMenu from '@/components/profile/ProfileMenu.vue'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Minu andmed: /participant-details (router guard: sisse logimata → login, admin → NotAuthorizedView).
// Vaikimisi ainult lugemiseks; "Muuda" avab vormi. E-post muutub ka sisselogimise e-postiks.
export default {
  name: 'ParticipantDetailsView',
  components: { InlineAlerts, ProfileMenu },
  data() {
    return {
      participant: null,
      participantForm: {
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
      },
      isEditing: false,
      isSending: false,
      successMessage: '',
      errorMessage: '',
    }
  },
  methods: {
    getMyParticipant() {
      UserService.sendGetMyParticipantRequest(SessionStorageService.getUserId())
        .then((response) => (this.participant = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    startEditing() {
      this.participantForm = {
        firstName: this.participant.firstName,
        lastName: this.participant.lastName,
        email: this.participant.email,
        phone: this.participant.phone,
      }
      this.successMessage = ''
      this.errorMessage = ''
      this.isEditing = true
    },

    // Laaditud väärtused jäävad alles (participant), vorm lihtsalt suletakse
    cancelEditing() {
      this.errorMessage = ''
      this.isEditing = false
    },

    updateProfile() {
      this.errorMessage = ''
      this.successMessage = ''
      this.checkParticipantFormForErrors()
      if (this.errorMessage !== '') {
        return
      }
      const profileUpdateRequest = {
        firstName: this.participantForm.firstName.trim(),
        lastName: this.participantForm.lastName.trim(),
        email: this.participantForm.email.trim(),
        phone: this.participantForm.phone.trim(),
      }
      this.isSending = true
      UserService.sendPutProfileRequest(SessionStorageService.getUserId(), profileUpdateRequest)
        .then(() => this.handleUpdateProfileResponse(profileUpdateRequest))
        .catch((error) => this.handleUpdateProfileError(error))
        .finally(() => (this.isSending = false))
    },

    handleUpdateProfileResponse(profileUpdateRequest) {
      this.participant = { ...this.participant, ...profileUpdateRequest }
      this.isEditing = false
      this.successMessage = this.$t('participantDetails.messages.saved')
    },

    // 403 EMAIL_TAKEN ja 400 → backendi teade, vorm jääb lahti
    handleUpdateProfileError(error) {
      const statusCode = error.response?.status
      if (statusCode === 400 || statusCode === 403) {
        this.errorMessage = error.response.data?.message ?? ''
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    checkParticipantFormForErrors() {
      const requiredValues = [
        this.participantForm.firstName,
        this.participantForm.lastName,
        this.participantForm.email,
        this.participantForm.phone,
      ]
      if (requiredValues.some((value) => value.trim() === '')) {
        this.errorMessage = this.$t('signup.validation.fillRequired')
      } else if (!EMAIL_PATTERN.test(this.participantForm.email.trim())) {
        this.errorMessage = this.$t('signup.validation.invalidEmail')
      }
    },
  },
  beforeMount() {
    this.getMyParticipant()
  },
}
</script>

<template>
  <div class="mx-auto w-full max-w-5xl px-4 py-6 sm:px-6 sm:py-10">
    <div class="lg:grid lg:grid-cols-[14rem_1fr] lg:items-start lg:gap-8">
      <ProfileMenu />

      <section v-if="participant" class="rounded-2xl border border-line bg-white p-5 sm:p-6">
        <h1 class="mb-4 text-xl sm:text-2xl">{{ $t('navbar.participantDetails') }}</h1>

        <dl v-if="!isEditing" class="mb-0 grid gap-4 sm:grid-cols-2">
          <div>
            <dt class="text-sm font-semibold text-muted">{{ $t('enquiryModal.firstName') }}</dt>
            <dd class="mt-0.5 mb-0 break-words text-ink">{{ participant.firstName || '—' }}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-muted">{{ $t('enquiryModal.lastName') }}</dt>
            <dd class="mt-0.5 mb-0 break-words text-ink">{{ participant.lastName || '—' }}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-muted">{{ $t('enquiryModal.phone') }}</dt>
            <dd class="mt-0.5 mb-0 break-words text-ink">{{ participant.phone || '—' }}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-muted">{{ $t('enquiryModal.email') }}</dt>
            <dd class="mt-0.5 mb-0 break-words text-ink">{{ participant.email }}</dd>
          </div>
        </dl>

        <div v-else class="grid gap-4 sm:grid-cols-2">
          <div>
            <label class="form-label" for="participant-first-name"
              >{{ $t('enquiryModal.firstName') }} *</label
            >
            <input
              v-model="participantForm.firstName"
              id="participant-first-name"
              class="form-control"
              type="text"
              maxlength="255"
              autocomplete="given-name"
            />
          </div>
          <div>
            <label class="form-label" for="participant-last-name"
              >{{ $t('enquiryModal.lastName') }} *</label
            >
            <input
              v-model="participantForm.lastName"
              id="participant-last-name"
              class="form-control"
              type="text"
              maxlength="255"
              autocomplete="family-name"
            />
          </div>
          <div>
            <label class="form-label" for="participant-phone"
              >{{ $t('enquiryModal.phone') }} *</label
            >
            <input
              v-model="participantForm.phone"
              id="participant-phone"
              class="form-control"
              type="tel"
              maxlength="20"
              autocomplete="tel"
            />
          </div>
          <div>
            <label class="form-label" for="participant-email"
              >{{ $t('enquiryModal.email') }} *</label
            >
            <input
              v-model="participantForm.email"
              id="participant-email"
              class="form-control"
              type="email"
              maxlength="255"
              autocomplete="email"
            />
            <div class="form-text">{{ $t('participantDetails.emailHint') }}</div>
          </div>
        </div>

        <div class="mt-5 flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-center">
          <button
            v-if="!isEditing"
            @click="startEditing"
            class="btn btn-primary w-full sm:w-auto"
            type="button"
          >
            {{ $t('participantDetails.edit') }}
          </button>
          <template v-else>
            <button
              @click="updateProfile"
              :disabled="isSending"
              class="btn btn-success w-full sm:w-auto"
              type="button"
            >
              {{ $t('participantDetails.save') }}
            </button>
            <button
              @click="cancelEditing"
              class="btn btn-outline-secondary w-full sm:w-auto"
              type="button"
            >
              {{ $t('participantDetails.cancel') }}
            </button>
          </template>
          <InlineAlerts
            :success-message="successMessage"
            :error-message="errorMessage"
            @event-success-message-closed="successMessage = ''"
            @event-error-message-closed="errorMessage = ''"
          />
        </div>
      </section>
    </div>
  </div>
</template>
