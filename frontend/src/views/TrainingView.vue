<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import TrainingTranslationService from '@/api-services/TrainingTranslationService.js'
import NavigationService from '@/services/NavigationService.js'
import RichTextContent from '@/components/common/RichTextContent.vue'

export default {
  name: 'TrainingView',
  components: { RichTextContent },
  data() {
    return {
      trainingId: 0,
      isMainLanguageFallback: false,
      translation: {
        title: '',
        description: '',
      },
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    // Parema veeru kaartide järjekord (tõlkevõtmed trainingView.sidebar.*)
    sidebarSections() {
      return ['trainingData', 'lecturer', 'upcomingCourses', 'calendar']
    },
  },
  watch: {
    '$route.query'() {
      this.loadView()
    },

    // Kasutajaliidese keele vahetus (navbar) → kuva koolituse info selles keeles
    contentLang() {
      this.getTrainingTranslation()
    },
  },
  methods: {
    loadView() {
      this.trainingId = Number(this.$route.query.trainingId ?? 0)
      if (this.trainingId === 0) {
        NavigationService.navigateToErrorView()
        return
      }
      this.getTrainingTranslation()
    },

    // Tõlge kasutajaliidese keeles; kui seda pole, siis põhikeele tõlge
    getTrainingTranslation() {
      TrainingService.sendGetTrainingTranslationsRequest(this.trainingId)
        .then((response) => this.handleGetTrainingTranslationsResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetTrainingTranslationsResponse(trainingTranslations) {
      const contentLangTranslation = trainingTranslations.find(
        (trainingTranslation) => trainingTranslation.languageCode === this.contentLang,
      )
      const mainLanguageTranslation = trainingTranslations.find(
        (trainingTranslation) => trainingTranslation.isMainLanguage,
      )
      const trainingTranslation = contentLangTranslation ?? mainLanguageTranslation
      if (trainingTranslation === undefined) {
        NavigationService.navigateToErrorView()
        return
      }
      this.isMainLanguageFallback = contentLangTranslation === undefined
      TrainingTranslationService.sendGetTrainingTranslationRequest(
        trainingTranslation.trainingTranslationId,
      )
        .then((response) => (this.translation = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },
  },
  beforeMount() {
    this.loadView()
  },
}
</script>

<template>
  <div class="container">
    <div class="row text-start">
      <!-- Vasak veerg: koolituse sisu -->
      <div class="col-lg-8">
        <fieldset class="border rounded p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('trainingView.legend') }}</legend>

          <p v-if="isMainLanguageFallback" class="small text-muted">
            {{ $t('trainingView.mainLanguageFallback') }}
          </p>

          <div class="fs-4 fw-semibold mb-3">{{ translation.title }}</div>
          <RichTextContent :html="translation.description" />
        </fieldset>
      </div>

      <!-- Parem veerg: kõrvalsektsioonid (praegu kohatäited) -->
      <div class="col-lg-4">
        <fieldset
          v-for="sidebarSection in sidebarSections"
          :key="sidebarSection"
          class="border rounded p-3 mb-4"
        >
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('trainingView.sidebar.' + sidebarSection) }}
          </legend>
          <p class="small text-muted mb-0">{{ $t('trainingView.sidebar.placeholder') }}</p>
        </fieldset>
      </div>
    </div>
  </div>
</template>
