<script>
import TrainingService from '@/api-services/TrainingService.js'
import NavigationService from '@/services/NavigationService.js'
import TrainingCard from '@/components/TrainingCard.vue'

export default {
  name: 'TrainingsView',
  components: { TrainingCard },
  data() {
    return {
      categoryId: 0,
      fundingTypeId: 0,
      limit: 3,
      page: 0,
      trainingLanguageId: 0,
      contentLang: localStorage.getItem('contentLang'),
      totalPages: 0,
      trainingSummaries: [
        {
          trainingId: 0,
          trainingLanguageCode: '',
          title: '',
          shortDescription: '',
          categoryId: 0,
          categoryName: '',
          isOrderable: false,
          isPromoted: false,
          fundingTypes: [
            {
              fundingTypeId: 0,
              fundingTypeName: '',
            },
          ],
        },
      ],
    }
  },
  methods: {
    getTrainings() {
      TrainingService.sendGetTrainingsRequest(
        this.categoryId,
        this.fundingTypeId,
        this.limit,
        this.page,
        this.trainingLanguageId,
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
    <div class="row">
      <div class="col-2">Siin on filtrid</div>
      <div class="col-10">
        <TrainingCard
          v-for="training in trainingSummaries"
          :key="training.trainingId"
          :training="training"
        />
      </div>
    </div>
  </div>
</template>
