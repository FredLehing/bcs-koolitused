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
    </h5>
    <div class="card-body d-flex justify-content-between">
      <p class="card-text">{{ training.shortDescription }}</p>
      <div>
        <span class="fi" :class="trainingLanguageFlag"></span>
        <a href="#" class="btn btn-primary">Vaata lähemalt</a>
      </div>
    </div>
  </div>
</template>
