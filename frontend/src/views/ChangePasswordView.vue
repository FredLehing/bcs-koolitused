<script>
import UserService from '@/api-services/UserService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import ProfileMenu from '@/components/profile/ProfileMenu.vue'

const PASSWORD_MIN_LENGTH = 8

// Parool: /change-password (router guard: sisse logimata → login). Kõigile sisseloginutele;
// osalejal on vasakul profiilimenüü, adminil ainult vormi kaart.
export default {
  name: 'ChangePasswordView',
  components: { InlineAlerts, ProfileMenu },
  data() {
    return {
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        newPasswordRepeat: '',
      },
      isSending: false,
      successMessage: '',
      errorMessage: '',
    }
  },
  computed: {
    userIsAdmin() {
      return SessionStorageService.userIsAdmin()
    },

    passwordMinLength() {
      return PASSWORD_MIN_LENGTH
    },
  },
  methods: {
    updatePassword() {
      this.errorMessage = ''
      this.successMessage = ''
      this.checkPasswordFormForErrors()
      if (this.errorMessage !== '') {
        return
      }
      this.isSending = true
      UserService.sendPutPasswordRequest(SessionStorageService.getUserId(), {
        currentPassword: this.passwordForm.currentPassword,
        newPassword: this.passwordForm.newPassword,
      })
        .then(() => this.handleUpdatePasswordResponse())
        .catch((error) => this.handleUpdatePasswordError(error))
        .finally(() => (this.isSending = false))
    },

    handleUpdatePasswordResponse() {
      this.passwordForm = { currentPassword: '', newPassword: '', newPasswordRepeat: '' }
      this.successMessage = this.$t('changePassword.messages.changed')
    },

    // 403 INCORRECT_PASSWORD ja 400 → backendi teade
    handleUpdatePasswordError(error) {
      const statusCode = error.response?.status
      if (statusCode === 400 || statusCode === 403) {
        this.errorMessage = error.response.data?.message ?? ''
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    checkPasswordFormForErrors() {
      const requiredValues = [
        this.passwordForm.currentPassword,
        this.passwordForm.newPassword,
        this.passwordForm.newPasswordRepeat,
      ]
      if (requiredValues.some((value) => value === '')) {
        this.errorMessage = this.$t('signup.validation.fillRequired')
      } else if (this.passwordForm.newPassword.length < PASSWORD_MIN_LENGTH) {
        this.errorMessage = this.$t('signup.validation.passwordTooShort', {
          length: PASSWORD_MIN_LENGTH,
        })
      } else if (this.passwordForm.newPassword !== this.passwordForm.newPasswordRepeat) {
        this.errorMessage = this.$t('signup.validation.passwordsDiffer')
      } else if (this.passwordForm.newPassword === this.passwordForm.currentPassword) {
        this.errorMessage = this.$t('changePassword.validation.sameAsCurrent')
      }
    },
  },
}
</script>

<template>
  <div class="mx-auto w-full max-w-5xl px-4 py-6 sm:px-6 sm:py-10">
    <div
      :class="
        userIsAdmin
          ? 'mx-auto max-w-lg'
          : 'lg:grid lg:grid-cols-[14rem_1fr] lg:items-start lg:gap-8'
      "
    >
      <ProfileMenu v-if="!userIsAdmin" />

      <section class="rounded-2xl border border-line bg-white p-5 sm:p-6">
        <h1 class="mb-4 text-xl sm:text-2xl">{{ $t('navbar.changePassword') }}</h1>

        <div class="flex max-w-md flex-col gap-4">
          <div>
            <label class="form-label" for="current-password">
              {{ $t('changePassword.currentPassword') }} *
            </label>
            <input
              v-model="passwordForm.currentPassword"
              id="current-password"
              class="form-control"
              type="password"
              maxlength="255"
              autocomplete="current-password"
            />
          </div>
          <div>
            <label class="form-label" for="new-password">
              {{ $t('changePassword.newPassword') }} *
            </label>
            <input
              v-model="passwordForm.newPassword"
              id="new-password"
              class="form-control"
              type="password"
              maxlength="255"
              autocomplete="new-password"
            />
            <div class="form-text">
              {{ $t('signup.passwordHint', { length: passwordMinLength }) }}
            </div>
          </div>
          <div>
            <label class="form-label" for="new-password-repeat">
              {{ $t('changePassword.newPasswordRepeat') }} *
            </label>
            <input
              v-model="passwordForm.newPasswordRepeat"
              id="new-password-repeat"
              class="form-control"
              type="password"
              maxlength="255"
              autocomplete="new-password"
              @keyup.enter="updatePassword"
            />
          </div>
        </div>

        <div class="mt-5 flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-center">
          <button
            @click="updatePassword"
            :disabled="isSending"
            class="btn btn-success w-full sm:w-auto"
            type="button"
          >
            {{ $t('changePassword.save') }}
          </button>
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
