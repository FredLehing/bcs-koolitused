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
  <div
    class="mx-auto flex w-full max-w-xl flex-1 flex-col items-center justify-center px-4 py-16 text-center"
  >
    <PhProhibit class="mb-4 text-red-600" :size="64" />
    <h1 class="mb-2 text-3xl font-extrabold tracking-tight">{{ $t('notAuthorized.title') }}</h1>
    <p class="text-lg text-muted">{{ $t('notAuthorized.message') }}</p>
    <div class="mt-6 flex w-full flex-col gap-3 sm:w-auto sm:flex-row">
      <button @click="pushToLoginView" type="button" class="btn btn-primary">
        {{ $t('notAuthorized.login') }}
      </button>
      <button @click="pushToHomeView" type="button" class="btn btn-outline-secondary">
        {{ $t('notAuthorized.home') }}
      </button>
    </div>
  </div>
</template>
