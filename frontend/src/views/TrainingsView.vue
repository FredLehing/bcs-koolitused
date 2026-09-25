<script>
import TrainingService from '@/api-services/TrainingService.js'
import NavigationService from "@/services/NavigationService.js";

export default {
  name: 'TrainingsView',
  data() {
    return {
      categoryId: 0,
      fundingTypeId: 0,
      limit: 3,
      page: 0,
      trainingLang: 'et',
      contentLang: 'et',
      totalPages: 0,
      trainingSummaries: [],
    }
  },
  methods: {
    getTrainings() {
      TrainingService.sendGetTrainingsRequest(
        this.categoryId,
        this.fundingTypeId,
        this.limit,
        this.page,
        this.trainingLang,
        this.contentLang,
      )
        .then((response) => this.handleGetTrainings(response))
        .catch(() => NavigationService.navigateToErrorView())
        .finally()
    },
    handleGetTrainings(response) {
      this.totalPages = response.data.totalPages
      this.trainingSummaries = response.data.trainingSummaries
    },
  },
  beforeMount() {
    this.getTrainings()
  },
}
</script>

<template>
  <div class="container">
    <h4>Koolitused on nüüd siin</h4>
    <div v-for="training in trainingSummaries" :key="training.trainingId">
      {{ training.title }}
    </div>
  </div>
</template>
