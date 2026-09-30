<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import TrainingTranslationService from '@/api-services/TrainingTranslationService.js'
import NavigationService from '@/services/NavigationService.js'
import RichTextContent from '@/components/common/RichTextContent.vue'
import EditTrainingLink from '@/components/common/EditTrainingLink.vue'
import LecturerCard from '@/components/common/LecturerCard.vue'

export default {
  name: 'TrainingView',
  components: { EditTrainingLink, LecturerCard, RichTextContent },
  data() {
    return {
      trainingId: 0,
      // Valikuline: kindla tõlge eelvaade (nt vormi "Vaata" nupp); 0 = tõlge kasutajaliidese keele järgi
      requestedTrainingTranslationId: 0,
      isMainLanguageFallback: false,
      translation: {
        trainingTranslationId: 0,
        title: '',
        description: '',
      },
      // Koolituse koolitajad järjekorras; kaardid laevad oma andmed ise
      lecturers: [],
      // Kustutatud / olematud koolitajad (kaart andis 404) — kui kõik on peidus, peidetakse ka jaotis
      notFoundLecturerIds: [],
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    // Parema veeru kaartide järjekord (tõlkevõtmed trainingView.sidebar.*); "lecturers" ainult koolitajatega
    sidebarSections() {
      const sidebarSections = ['trainingData', 'lecturers', 'upcomingCourses', 'calendar']
      return this.hasVisibleLecturers
        ? sidebarSections
        : sidebarSections.filter((sidebarSection) => sidebarSection !== 'lecturers')
    },

    hasVisibleLecturers() {
      return this.lecturers.some(
        (lecturer) => !this.notFoundLecturerIds.includes(lecturer.lecturerId),
      )
    },
  },
  watch: {
    '$route.query'() {
      this.loadView()
    },

    // Kasutajaliidese keele vahetus (navbar) → kuva koolituse info selles keeles.
    // Kindla tõlke parameeter eemaldatakse URL-ist (query jälgija laadib vaate uuesti).
    contentLang() {
      if (this.requestedTrainingTranslationId !== 0) {
        NavigationService.replaceTrainingView(this.trainingId)
      } else {
        this.getTrainingTranslation()
      }
    },
  },
  methods: {
    loadView() {
      this.trainingId = Number(this.$route.query.trainingId ?? 0)
      this.requestedTrainingTranslationId = Number(this.$route.query.trainingTranslationId ?? 0)
      if (this.trainingId === 0) {
        NavigationService.navigateToErrorView()
        return
      }
      this.getTrainingTranslation()
      this.getTrainingLecturers()
    },

    // Koolitajad on lisainfo — vea korral jaotist lihtsalt ei kuvata
    getTrainingLecturers() {
      this.notFoundLecturerIds = []
      TrainingService.sendGetTrainingRequest(this.trainingId)
        .then((response) => (this.lecturers = response.data.lecturers))
        .catch(() => (this.lecturers = []))
    },

    handleLecturerNotFound(lecturerId) {
      this.notFoundLecturerIds.push(lecturerId)
    },

    // Valiku järjekord: URL-is antud tõlge (kui kuulub sellele koolitusele) → kasutajaliidese keele
    // tõlge → põhikeele tõlge
    getTrainingTranslation() {
      TrainingService.sendGetTrainingTranslationsRequest(this.trainingId)
        .then((response) => this.handleGetTrainingTranslationsResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetTrainingTranslationsResponse(trainingTranslations) {
      const requestedTranslation = trainingTranslations.find(
        (trainingTranslation) =>
          trainingTranslation.trainingTranslationId === this.requestedTrainingTranslationId,
      )
      const contentLangTranslation = trainingTranslations.find(
        (trainingTranslation) => trainingTranslation.languageCode === this.contentLang,
      )
      const mainLanguageTranslation = trainingTranslations.find(
        (trainingTranslation) => trainingTranslation.isMainLanguage,
      )
      const trainingTranslation =
        requestedTranslation ?? contentLangTranslation ?? mainLanguageTranslation
      if (trainingTranslation === undefined) {
        NavigationService.navigateToErrorView()
        return
      }
      this.isMainLanguageFallback =
        requestedTranslation === undefined && contentLangTranslation === undefined
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
        <fieldset class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('trainingView.legend') }}</legend>

          <p v-if="isMainLanguageFallback" class="small text-muted">
            {{ $t('trainingView.mainLanguageFallback') }}
          </p>

          <div class="d-flex justify-content-between align-items-center gap-2 mb-3">
            <div class="fs-4 fw-semibold">{{ translation.title }}</div>
            <EditTrainingLink
              v-if="translation.trainingTranslationId !== 0"
              :training-id="trainingId"
              :training-translation-id="translation.trainingTranslationId"
            />
          </div>
          <RichTextContent :html="translation.description" />
        </fieldset>
      </div>

      <!-- Parem veerg: kõrvalsektsioonid (koolitajad; ülejäänud praegu kohatäited) -->
      <div class="col-lg-4">
        <fieldset
          v-for="sidebarSection in sidebarSections"
          :key="sidebarSection"
          class="border rounded bg-body p-3 mb-4"
        >
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('trainingView.sidebar.' + sidebarSection) }}
          </legend>
          <template v-if="sidebarSection === 'lecturers'">
            <LecturerCard
              v-for="lecturer in lecturers"
              :key="lecturer.lecturerId"
              :lecturer-id="lecturer.lecturerId"
              class="lecturer-card"
              @event-lecturer-not-found="handleLecturerNotFound"
            />
          </template>
          <p v-else class="small text-muted mb-0">{{ $t('trainingView.sidebar.placeholder') }}</p>
        </fieldset>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Mitme koolitaja kaardid üksteise all */
.lecturer-card + .lecturer-card {
  margin-top: 1rem;
}
</style>
