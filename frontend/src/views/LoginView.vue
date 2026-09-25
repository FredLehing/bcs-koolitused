<script>
import LoginService from '@/api-services/LoginService.js'
import NavigationService from '@/services/NavigationService.js'

export default {
  name: 'LoginView',
  data() {
    return {
      email: '',
      password: '',
      errorMessage: '',
    }
  },
  methods: {
    login() {
      this.errorMessage = ''
      if (this.email === '' || this.password === '') {
        this.errorMessage = 'Täida kõik väljad'
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
      NavigationService.navigateToHomeView()
    },

    handleLoginError(loginError) {
      this.errorMessage = loginError.response.data.message
    },
  },
}
</script>

<template>
  <div class="container flex-grow-1 d-flex flex-column justify-content-center">
    <div class="row">
      <div class="col-6"></div>

      <div class="col-6 text-center">
        <h1>Sisselogimine</h1>
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
          <label for="floatingPassword">Parool</label>
        </div>

        <div class="form-check mb-3 d-inline-block">
          <input type="checkbox" class="form-check-input" id="rememberMe" />
          <label class="form-check-label" for="rememberMe">Jäta mind meelde</label>
        </div>
        <div></div>
        <button @click="login" type="button" class="btn btn-primary">Logi sisse</button>

        <div class="mt-2">
          <a href="#">Unustasid salasõna?</a>
        </div>
      </div>
    </div>
  </div>
</template>
