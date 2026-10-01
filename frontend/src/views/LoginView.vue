<script>
import LoginService from '@/api-services/LoginService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'

export default {
  name: 'LoginView',
  data() {
    return {
      email: '',
      password: '',
      errorMessage: '',
    }
  },
  computed: {
    // Rada, kuhu pärast sisselogimist minnakse (nt registreerumine); '' = avaleht või admini vaade
    redirect() {
      return this.$route.query.redirect ?? ''
    },
  },
  methods: {
    login() {
      this.errorMessage = ''
      if (this.email === '' || this.password === '') {
        this.errorMessage = this.$t('login.errorMessage')
      } else {
        LoginService.sendLoginRequest(this.email, this.password)
          .then((response) => this.handleLoginResponse(response.data))
          .catch((error) => this.handleLoginError(error))
          .finally()
      }
    },
    handleLoginResponse(loginResponse) {
      sessionStorage.setItem('userId', loginResponse.userId)
      sessionStorage.setItem('roleName', loginResponse.roleName)
      // Admin ilma redirectita → admin-menüü esimene vaade (koolituste päringud)
      if (SessionStorageService.userIsAdmin() && !NavigationService.isInternalPath(this.redirect)) {
        NavigationService.navigateToAdminEnquiriesView()
      } else {
        NavigationService.navigateToRedirectOrHomeView(this.redirect)
      }
    },

    handleLoginError(loginError) {
      if (loginError.response.data.errorCode === 'INCORRECT_CREDENTIALS') {
        this.errorMessage = this.$t('login.incorrectCredentials')
      } else {
        this.errorMessage = loginError.response.data.message
      }
    },
  },
}
</script>

<template>
  <div class="flex flex-1 items-center justify-center px-4 py-10 sm:py-16">
    <div
      class="flex w-full max-w-4xl overflow-hidden rounded-3xl border border-line bg-white shadow-xl shadow-navy/5"
    >
      <!-- Brändipaneel logo triipudega (ainult laial ekraanil) -->
      <div
        class="relative hidden w-5/12 flex-col justify-between overflow-hidden bg-navy p-10 text-white md:flex"
        aria-hidden="true"
      >
        <p class="font-display text-2xl leading-snug font-bold">{{ $t('homeView.subtitle') }}</p>
        <div class="-mr-10 flex flex-col gap-4">
          <div class="h-9 w-[90%] rounded-l-full bg-brand-600"></div>
          <div class="ml-[30%] h-9 w-[70%] rounded-l-full bg-brand-300"></div>
          <div class="h-9 w-full rounded-l-full bg-brand-600"></div>
        </div>
      </div>

      <form class="flex flex-1 flex-col gap-5 p-6 sm:p-10" @submit.prevent="login">
        <h1 class="text-3xl font-extrabold tracking-tight">{{ $t('login.logIn') }}</h1>
        <div v-if="redirect" class="alert alert-info">{{ $t('login.redirectInfo') }}</div>
        <div v-if="errorMessage" class="alert alert-danger" role="alert">{{ errorMessage }}</div>

        <div>
          <label class="form-label" for="login-email">{{ $t('login.email') }}</label>
          <input
            v-model="email"
            id="login-email"
            type="email"
            class="form-control"
            autocomplete="username"
          />
        </div>
        <div>
          <label class="form-label" for="login-password">{{ $t('login.password') }}</label>
          <input
            v-model="password"
            id="login-password"
            type="password"
            class="form-control"
            autocomplete="current-password"
          />
        </div>
        <div class="flex flex-wrap items-center justify-between gap-2">
          <div class="form-check">
            <input type="checkbox" class="form-check-input" id="rememberMe" />
            <label class="form-check-label" for="rememberMe">{{ $t('login.rememberMe') }}</label>
          </div>
          <a href="#" class="text-[15px] font-semibold">{{ $t('login.forgotPassword') }}</a>
        </div>
        <button type="submit" class="btn btn-primary btn-lg w-full">
          {{ $t('login.logIn') }}
        </button>
        <p class="text-center text-muted">
          {{ $t('login.noAccount') }}
          <RouterLink
            :to="{ name: 'signupRoute', query: redirect ? { redirect: redirect } : {} }"
            class="font-semibold"
          >
            {{ $t('navbar.signup') }}
          </RouterLink>
        </p>
      </form>
    </div>
  </div>
</template>
