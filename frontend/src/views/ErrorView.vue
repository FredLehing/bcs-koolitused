<script>
import NavigationService from '@/services/NavigationService.js'

export default {
  name: 'ErrorView',

  methods: {
    pushToTrainingsView() {
      NavigationService.navigateToTrainingsView()
    },
  },
}
</script>

<template>
  <div
    class="mx-auto flex w-full max-w-xl flex-1 flex-col items-center justify-center px-4 py-16 text-center"
  >
    <p class="text-lg text-muted">{{ $t('error.message') }}</p>
    <button @click="pushToTrainingsView" type="button" class="btn btn-primary mt-6">
      {{ $t('error.backToTrainings') }}
    </button>
  </div>
</template>
