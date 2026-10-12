<script>
import { PhCaretRight } from '@phosphor-icons/vue'
import BackLink from '@/components/common/BackLink.vue'
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import LecturerService from '@/api-services/LecturerService.js'
import NavigationService from '@/services/NavigationService.js'
import LecturerAvatar from '@/components/common/LecturerAvatar.vue'
import RichTextContent from '@/components/common/RichTextContent.vue'

export default {
  name: 'LecturerView',
  components: { BackLink, LecturerAvatar, PhCaretRight, RichTextContent },
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
  <div class="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 sm:py-10">
    <BackLink :fallback="{ name: 'lecturersRoute' }" />

    <div v-if="lecturerProfile" class="flex flex-col gap-6 md:flex-row md:items-start md:gap-8">
      <LecturerAvatar
        :lecturer-id="lecturerProfile.lecturerId"
        :photo-version="lecturerProfile.photoVersion"
        :alt="lecturerProfile.fullName"
        :size="240"
        shape="rounded"
        class="aspect-square h-auto! max-w-full md:shrink-0"
      />
      <div class="min-w-0 flex-1 rounded-2xl border border-line bg-white p-5 sm:p-8">
        <h1 class="mb-1 text-3xl font-extrabold tracking-tight sm:text-4xl">
          {{ lecturerProfile.fullName }}
        </h1>
        <p class="mb-4 text-lg text-muted">{{ lecturerProfile.title }}</p>
        <p class="mb-4 text-lg font-semibold text-navy">{{ lecturerProfile.shortDescription }}</p>
        <RichTextContent :html="lecturerProfile.description" class="max-w-prose" />

        <template v-if="lecturerProfile.trainings.length > 0">
          <h2 class="mt-8 mb-2 text-xl font-bold">{{ $t('lecturer.trainings') }}</h2>
          <ul class="flex flex-col">
            <li
              v-for="training in lecturerProfile.trainings"
              :key="training.trainingId"
              class="border-b border-line last:border-0"
            >
              <RouterLink
                :to="{
                  name: 'trainingRoute',
                  query: {
                    returnTo: $route.fullPath,
                    trainingId: training.trainingId,
                    trainingTranslationId: training.trainingTranslationId,
                  },
                }"
                class="flex min-h-11 items-center justify-between gap-3 py-2 font-semibold"
              >
                {{ training.title }}
                <PhCaretRight :size="18" class="shrink-0 text-brand-600" />
              </RouterLink>
            </li>
          </ul>
        </template>
      </div>
    </div>
  </div>
</template>
