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
    <h1>{{ $t('notAuthorized.title') }}</h1>
    <p>{{ $t('notAuthorized.message') }}</p>
    <div class="d-flex justify-content-center gap-3 mt-3">
      <button @click="pushToLoginView" type="button" class="btn btn-primary">
        {{ $t('notAuthorized.login') }}
      </button>
      <button @click="pushToHomeView" type="button" class="btn btn-outline-secondary">
        {{ $t('notAuthorized.home') }}
      </button>
    </div>
  </div>
</template>
