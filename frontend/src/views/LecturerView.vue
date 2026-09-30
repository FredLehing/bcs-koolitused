<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import LecturerService from '@/api-services/LecturerService.js'
import NavigationService from '@/services/NavigationService.js'
import LecturerAvatar from '@/components/common/LecturerAvatar.vue'
import RichTextContent from '@/components/common/RichTextContent.vue'

export default {
  name: 'LecturerView',
  components: { LecturerAvatar, RichTextContent },
  data() {
    return {
      lecturerId: 0,
      lecturerProfile: null,
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),
  },
  watch: {
    '$route.query'() {
      this.loadView()
    },

    contentLang() {
      this.getLecturerProfile()
    },
  },
  methods: {
    loadView() {
      this.lecturerId = Number(this.$route.query.lecturerId ?? 0)
      if (this.lecturerId === 0) {
        NavigationService.navigateToErrorView()
        return
      }
      this.getLecturerProfile()
    },

    // Kustutatud või olematu koolitaja (404) → üldine veavaade
    getLecturerProfile() {
      LecturerService.sendGetLecturerProfileRequest(this.lecturerId, this.contentLang)
        .then((response) => (this.lecturerProfile = response.data))
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
    <RouterLink :to="{ name: 'lecturersRoute' }" class="d-inline-block mb-3">
      ← {{ $t('lecturer.backToLecturers') }}
    </RouterLink>

    <div v-if="lecturerProfile" class="row g-4 mb-5 text-start">
      <div class="col-md-4 col-lg-3">
        <LecturerAvatar
          :lecturer-id="lecturerProfile.lecturerId"
          :photo-version="lecturerProfile.photoVersion"
          :alt="lecturerProfile.fullName"
          :size="240"
          shape="rounded"
          class="lecturer-photo"
        />
      </div>
      <div class="col-md-8 col-lg-9">
        <h1 class="mb-1">{{ lecturerProfile.fullName }}</h1>
        <p class="text-secondary fs-5">{{ lecturerProfile.title }}</p>
        <p class="lead">{{ lecturerProfile.shortDescription }}</p>
        <RichTextContent :html="lecturerProfile.description" />

        <template v-if="lecturerProfile.trainings.length > 0">
          <h2 class="h4 mt-4">{{ $t('lecturer.trainings') }}</h2>
          <ul>
            <li v-for="training in lecturerProfile.trainings" :key="training.trainingId">
              <RouterLink
                :to="{
                  name: 'trainingRoute',
                  query: {
                    trainingId: training.trainingId,
                    trainingTranslationId: training.trainingTranslationId,
                  },
                }"
              >
                {{ training.title }}
              </RouterLink>
            </li>
          </ul>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Suur pilt kitsal ekraanil ei tohi veerust välja minna */
.lecturer-photo {
  max-width: 100%;
  height: auto !important;
  aspect-ratio: 1;
}
</style>
