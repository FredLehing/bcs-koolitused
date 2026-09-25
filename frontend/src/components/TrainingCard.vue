<script>
export default {
  name: 'TrainingCard',
  props: {
    training: Object,
  },
  computed: {
    trainingLanguageFlag() {
      return this.training.trainingLanguageCode === 'et' ? 'fi-ee' : 'fi-gb'
    },
  },
}
</script>

<template>
  <div class="card mb-4">
    <h5 class="card-header">
      {{ training.title }}
      <span class="fi" :class="trainingLanguageFlag"></span>
    </h5>
    <div class="card-body">
      <p class="card-text">{{ training.shortDescription }}</p>
      <a href="#" class="btn btn-primary">Vaata lähemalt</a>
    </div>
  </div>
</template>
