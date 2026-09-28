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
    <h5 class="card-header fs-3">
      {{ training.title }}
    </h5>
    <div class="card-body d-flex justify-content-between fs-5">
      <div>
        <p class="card-text">{{ training.shortDescription }}</p>
        <span class="badge text-bg-primary">{{ training.categoryName }}</span>
        <div class="mt-2 d-flex gap-2 fs-5">
          <span
            v-for="fundingType in training.fundingTypes"
            :key="fundingType.fundingTypeId"
            class="badge text-bg-primary"
            >{{ fundingType.fundingTypeName }}</span
          >
        </div>
      </div>
      <div class="d-flex align-items-center flex-column gap-3 fs-5">
        <span class="fi fs-3" :class="trainingLanguageFlag"></span>
        <span v-if="training.isOrderable" class="badge text-bg-success">Tellitav</span>
        <a href="#" class="btn btn-primary">Vaata lähemalt</a>
      </div>
    </div>
  </div>
</template>
