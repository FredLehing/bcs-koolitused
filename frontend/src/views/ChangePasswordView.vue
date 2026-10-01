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
  <div class="container">
    <div class="row g-4 text-start mb-5" :class="{ 'justify-content-center': userIsAdmin }">
      <div v-if="!userIsAdmin" class="col-lg-3">
        <ProfileMenu />
      </div>

      <div :class="userIsAdmin ? 'col-md-8 col-lg-5' : 'col-lg-9'">
        <fieldset class="border rounded bg-body p-3">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('navbar.changePassword') }}</legend>

          <div class="d-flex flex-column gap-3" :class="{ 'col-lg-7': !userIsAdmin }">
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

          <div class="d-flex flex-wrap align-items-center gap-2 mt-3">
            <button
              @click="updatePassword"
              :disabled="isSending"
              class="btn btn-success"
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
        </fieldset>
      </div>
    </div>
  </div>
</template>
