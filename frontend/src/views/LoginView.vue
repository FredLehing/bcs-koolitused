<template>
  <div class="container">
    <div class="row">
      <div class="col-6"></div>

      <div class="col-6 text-center">
        <h1>Sisselogimine</h1>
        <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

        <div class="row mb-3">
          <label class="col-3 col-form-label">Email</label>
          <div class="col-6">
            <input v-model="email" type="email" class="form-control" />
          </div>
        </div>

        <div class="row mb-3">
          <label class="col-3 col-form-label">Parool</label>
          <div class="col-6">
            <input v-model="password" type="password" class="form-control" />
          </div>
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

<script>
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
      }
    },
  },
}
</script>
