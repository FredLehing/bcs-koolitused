<script>
import UserService from '@/api-services/UserService.js'
import NavigationService from '@/services/NavigationService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'

const PASSWORD_MIN_LENGTH = 8
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Konto loomine: /signup?redirect=... — pärast loomist on kasutaja kohe sisse logitud
export default {
  name: 'SignupView',
  components: { AlertDanger },
  data() {
    return {
      signup: {
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        password: '',
        passwordRepeat: '',
      },
      errorMessage: '',
      isEmailTaken: false,
      isSending: false,
    }
  },
  computed: {
    // Rada, kuhu pärast konto loomist minnakse (nt registreerumine); '' = avaleht
    redirect() {
      return this.$route.query.redirect ?? ''
    },

    passwordMinLength() {
      return PASSWORD_MIN_LENGTH
    },
  },
  methods: {
    addUser() {
      this.errorMessage = ''
      this.isEmailTaken = false
      this.checkSignupForErrors()
      if (this.errorMessage !== '') {
        return
      }
      this.isSending = true
      UserService.sendPostUserRequest({
        firstName: this.signup.firstName.trim(),
        lastName: this.signup.lastName.trim(),
        email: this.signup.email.trim(),
        phone: this.signup.phone.trim(),
        password: this.signup.password,
      })
        .then((response) => this.handleAddUserResponse(response.data))
        .catch((error) => this.handleAddUserError(error))
        .finally(() => (this.isSending = false))
    },

    handleAddUserResponse(loginResponse) {
      sessionStorage.setItem('userId', loginResponse.userId)
      sessionStorage.setItem('roleName', loginResponse.roleName)
      NavigationService.navigateToRedirectOrHomeView(this.redirect)
    },

    // 403 EMAIL_TAKEN → teade + link sisselogimisele; 400 → backendi teade
    handleAddUserError(error) {
      const statusCode = error.response?.status
      if (statusCode === 403 && error.response.data?.errorCode === 'EMAIL_TAKEN') {
        this.isEmailTaken = true
        this.errorMessage = error.response.data.message
      } else if (statusCode === 400) {
        this.errorMessage =
          error.response.data?.message ?? this.$t('signup.validation.fillRequired')
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    checkSignupForErrors() {
      const requiredValues = [
        this.signup.firstName,
        this.signup.lastName,
        this.signup.email,
        this.signup.phone,
        this.signup.password,
        this.signup.passwordRepeat,
      ]
      if (requiredValues.some((value) => value.trim() === '')) {
        this.errorMessage = this.$t('signup.validation.fillRequired')
      } else if (!EMAIL_PATTERN.test(this.signup.email.trim())) {
        this.errorMessage = this.$t('signup.validation.invalidEmail')
      } else if (this.signup.password.length < PASSWORD_MIN_LENGTH) {
        this.errorMessage = this.$t('signup.validation.passwordTooShort', {
          length: PASSWORD_MIN_LENGTH,
        })
      } else if (this.signup.password !== this.signup.passwordRepeat) {
        this.errorMessage = this.$t('signup.validation.passwordsDiffer')
      }
    },
  },
}
</script>

<template>
  <div class="flex flex-1 items-center justify-center px-4 py-10 sm:py-16">
    <div
      class="w-full max-w-2xl rounded-3xl border border-line bg-white p-6 shadow-xl shadow-navy/5 sm:p-10"
    >
      <h1 class="mb-5 text-3xl font-extrabold tracking-tight">{{ $t('navbar.signup') }}</h1>
      <div v-if="redirect" class="alert alert-info mb-5">{{ $t('signup.redirectInfo') }}</div>

      <form class="flex flex-col gap-5" novalidate @submit.prevent="addUser">
        <div class="grid grid-cols-1 gap-5 sm:grid-cols-2">
          <div>
            <label class="form-label" for="signup-first-name"
              >{{ $t('enquiryModal.firstName') }} *</label
            >
            <input
              v-model="signup.firstName"
              id="signup-first-name"
              class="form-control"
              type="text"
              maxlength="255"
              autocomplete="given-name"
            />
          </div>
          <div>
            <label class="form-label" for="signup-last-name"
              >{{ $t('enquiryModal.lastName') }} *</label
            >
            <input
              v-model="signup.lastName"
              id="signup-last-name"
              class="form-control"
              type="text"
              maxlength="255"
              autocomplete="family-name"
            />
          </div>
          <div>
            <label class="form-label" for="signup-email">{{ $t('enquiryModal.email') }} *</label>
            <input
              v-model="signup.email"
              id="signup-email"
              class="form-control"
              type="email"
              maxlength="255"
              autocomplete="email"
            />
          </div>
          <div>
            <label class="form-label" for="signup-phone">{{ $t('enquiryModal.phone') }} *</label>
            <input
              v-model="signup.phone"
              id="signup-phone"
              class="form-control"
              type="tel"
              maxlength="20"
              autocomplete="tel"
            />
          </div>
          <div>
            <label class="form-label" for="signup-password">{{ $t('login.password') }} *</label>
            <input
              v-model="signup.password"
              id="signup-password"
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
            <label class="form-label" for="signup-password-repeat">
              {{ $t('signup.passwordRepeat') }} *
            </label>
            <input
              v-model="signup.passwordRepeat"
              id="signup-password-repeat"
              class="form-control"
              type="password"
              maxlength="255"
              autocomplete="new-password"
            />
          </div>
        </div>

        <div v-if="errorMessage" class="flex flex-col items-start gap-2">
          <AlertDanger :error-message="errorMessage" class="w-full" />
          <RouterLink
            v-if="isEmailTaken"
            :to="{ name: 'loginRoute', query: redirect ? { redirect: redirect } : {} }"
            class="inline-flex min-h-11 items-center font-semibold"
          >
            {{ $t('navbar.login') }}
          </RouterLink>
        </div>

        <button :disabled="isSending" class="btn btn-primary btn-lg w-full" type="submit">
          {{ $t('navbar.signup') }}
        </button>
        <p class="text-center">
          <RouterLink
            :to="{ name: 'loginRoute', query: redirect ? { redirect: redirect } : {} }"
            class="font-semibold"
          >
            {{ $t('signup.haveAccount') }}
          </RouterLink>
        </p>
      </form>
    </div>
  </div>
</template>
