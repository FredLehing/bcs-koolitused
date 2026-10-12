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
  <div class="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 sm:py-10">
    <h1 class="mb-2 text-3xl font-extrabold tracking-tight sm:text-4xl">
      {{ $t('lecturers.title') }}
    </h1>
    <p class="mb-8 max-w-3xl text-lg text-muted">{{ $t('lecturers.intro') }}</p>

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 sm:gap-5 lg:grid-cols-3 xl:grid-cols-4">
      <LecturerTile
        v-for="lecturerSummary in lecturerSummaries"
        :key="lecturerSummary.lecturerId"
        :lecturer-summary="lecturerSummary"
      />
    </div>

    <p v-if="isLoaded && lecturerSummaries.length === 0" class="text-muted">
      {{ $t('lecturers.empty') }}
    </p>
  </div>
</template>
