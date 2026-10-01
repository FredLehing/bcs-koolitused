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
  <div class="container flex-grow-1 d-flex flex-column justify-content-center">
    <div class="row align-items-center">
      <div class="col-6">
        <img class="img-fluid" src="@/assets/programming.gif" alt="koodimine" />
      </div>
      <div class="col-6 text-center">
        <div v-if="redirect" class="alert alert-info w-75 mx-auto">
          {{ $t('login.redirectInfo') }}
        </div>
        <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

        <div class="form-floating mb-3 w-75 mx-auto">
          <input
            v-model="email"
            type="email"
            class="form-control"
            id="floatingInput"
            placeholder="Email"
          />
          <label for="floatingInput">Email</label>
        </div>
        <div class="form-floating mb-3 w-75 mx-auto">
          <input
            v-model="password"
            type="password"
            class="form-control"
            id="floatingPassword"
            placeholder="Parool"
          />
          <label for="floatingPassword">{{ $t('login.password') }}</label>
        </div>
        <div class="d-flex justify-content-between w-75 mx-auto">
          <div class="form-check mb-3 d-inline-block">
            <input type="checkbox" class="form-check-input" id="rememberMe" />
            <label class="form-check-label" for="rememberMe">{{ $t('login.rememberMe') }}</label>
          </div>
          <div>
            <a href="#">{{ $t('login.forgotPassword') }}</a>
          </div>
        </div>
        <div></div>
        <button @click="login" type="button" class="btn btn-primary w-75 mx-auto text-uppercase">
          {{ $t('login.logIn') }}
        </button>
        <p class="mt-3">
          {{ $t('login.noAccount') }}
          <RouterLink :to="{ name: 'signupRoute', query: redirect ? { redirect: redirect } : {} }">
            {{ $t('navbar.signup') }}
          </RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
