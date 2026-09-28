<script>
import { PhProhibit } from '@phosphor-icons/vue'
import NavigationService from '@/services/NavigationService.js'

export default {
  name: 'NotAuthorizedView',
  components: { PhProhibit },
  methods: {
    pushToLoginView() {
      NavigationService.navigateToLoginView()
    },

    pushToHomeView() {
      NavigationService.navigateToHomeView()
    },
  },
}
</script>

<template>
  <div class="container text-center">
    <PhProhibit class="text-danger mb-3" :size="64" />
    <h1>Ligipääs keelatud</h1>
    <p>Selle lehe vaatamiseks pead olema administraatorina sisse logitud.</p>
    <div class="d-flex justify-content-center gap-3 mt-3">
      <button @click="pushToLoginView" type="button" class="btn btn-primary">Logi sisse</button>
      <button @click="pushToHomeView" type="button" class="btn btn-outline-secondary">
        Avalehele
      </button>
    </div>
  </div>
</template>
