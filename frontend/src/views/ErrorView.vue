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
  <div class="container text-center">
    <p>{{ $t('error.message') }}</p>
    <button @click="pushToTrainingsView" type="button" class="btn btn-primary mt-3">
      {{ $t('error.backToTrainings') }}
    </button>
  </div>
</template>
