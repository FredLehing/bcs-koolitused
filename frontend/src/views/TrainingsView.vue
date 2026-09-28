<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
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
      totalPages: 0,
      trainings: [
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
  computed: {
    // Kasutajaliidese keel (navbaris valitud) — sellega küsitakse koolituste tõlgitud väljad
    ...mapState(useLanguageStore, ['contentLang']),
  },
  watch: {
    // Keele vahetus navbaris → laadi koolitused uues keeles (filtrid ja lehekülg jäävad alles)
    contentLang() {
      this.getTrainings()
    },
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
        .then((response) => this.handleGetTrainingsResponse(response))
        .catch(() => NavigationService.navigateToErrorView())
        .finally()
    },
    handleGetTrainingsResponse(response) {
      this.totalPages = response.data.totalPages
      this.trainings = response.data.trainingSummaries
    },
    handlePageClick(pagination) {
      this.page = pagination - 1
      this.getTrainings()
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
          v-for="training in trainings"
          :key="training.trainingId"
          :training="training"
        />
        <nav aria-label="...">
          <ul class="pagination justify-content-center">
            <li class="page-item"><a href="#" class="page-link" :class="{disabled: page === 0 }" @click.prevent="handlePageClick(page)">Eelmine</a></li>

            <li
              v-for="totalPage in totalPages"
              :key="totalPage"
              class="page-item"
              :class="{ active: totalPage === page + 1 }"
            >
              <a class="page-link" href="#" @click.prevent="handlePageClick(totalPage)">{{totalPage}}</a>
            </li>
            <li class="page-item">
              <a class="page-link" href="#" :class="{disabled: page === totalPages - 1}" @click.prevent="handlePageClick(page + 2)"
                >Järgmine</a
              >
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>
