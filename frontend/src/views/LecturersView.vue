<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import LecturerService from '@/api-services/LecturerService.js'
import NavigationService from '@/services/NavigationService.js'
import LecturerTile from '@/components/common/LecturerTile.vue'

export default {
  name: 'LecturersView',
  components: { LecturerTile },
  data() {
    return {
      isLoaded: false,
      lecturerSummaries: [],
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),
  },
  watch: {
    contentLang() {
      this.getLecturerSummaries()
    },
  },
  methods: {
    getLecturerSummaries() {
      LecturerService.sendGetLecturerSummariesRequest(this.contentLang)
        .then((response) => this.handleGetLecturerSummariesResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetLecturerSummariesResponse(lecturerSummaries) {
      this.lecturerSummaries = lecturerSummaries
      this.isLoaded = true
    },
  },
  beforeMount() {
    this.getLecturerSummaries()
  },
}
</script>

<template>
  <div class="container">
    <h1 class="mb-2">{{ $t('lecturers.title') }}</h1>
    <p class="text-secondary mb-4">{{ $t('lecturers.intro') }}</p>

    <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 row-cols-xl-4 g-4 mb-5">
      <div
        v-for="lecturerSummary in lecturerSummaries"
        :key="lecturerSummary.lecturerId"
        class="col"
      >
        <LecturerTile :lecturer-summary="lecturerSummary" />
      </div>
    </div>

    <p v-if="isLoaded && lecturerSummaries.length === 0" class="text-secondary">
      {{ $t('lecturers.empty') }}
    </p>
  </div>
</template>
