<script>
import { mapState } from 'pinia'
import { PhCaretRight, PhDownloadSimple, PhStar } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import FormatService from '@/services/FormatService.js'
import NavigationService from '@/services/NavigationService.js'
import BackLink from '@/components/common/BackLink.vue'
import AlertDanger from '@/components/common/AlertDanger.vue'
import EditTrainingLink from '@/components/common/EditTrainingLink.vue'
import FlagIcon from '@/components/common/FlagIcon.vue'
import LecturerCard from '@/components/common/LecturerCard.vue'
import RichTextContent from '@/components/common/RichTextContent.vue'

export default {
  name: 'TrainingView',
  components: {
    BackLink,
    AlertDanger,
    EditTrainingLink,
    FlagIcon,
    LecturerCard,
    RichTextContent,
    PhCaretRight,
    PhDownloadSimple,
    PhStar,
  },
  data() {
    return {
      trainingId: 0,
      requestedTrainingTranslationId: 0,
      trainingPage: null,
      requestNumber: 0,
      errorMessage: '',
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),
    hasVisibleLecturers() {
      return this.trainingPage.lecturers.length > 0
    },
    fundingTypeNames() {
      return this.trainingPage.fundingTypes
        .map((fundingType) => fundingType.fundingTypeName)
        .join(', ')
    },
    curriculumUrl() {
      return TrainingService.getCurriculumUrl(this.trainingPage.trainingTranslationId)
    },
    curriculumSize() {
      const size = this.trainingPage.curriculumFileSize
      if (size === null) return ''
      return (
        new Intl.NumberFormat(this.contentLang, { maximumFractionDigits: 1 }).format(
          size / (1024 * 1024),
        ) + ' MB'
      )
    },
  },
  watch: {
    '$route.query'() {
      this.loadView()
    },
    contentLang() {
      if (this.requestedTrainingTranslationId !== 0) {
        NavigationService.replaceTrainingView(this.trainingId)
      } else {
        this.getTrainingPage()
      }
    },
  },
  methods: {
    loadView() {
      this.trainingId = Number(this.$route.query.trainingId ?? 0)
      this.requestedTrainingTranslationId = Number(this.$route.query.trainingTranslationId ?? 0)
      this.getTrainingPage()
    },
    getTrainingPage() {
      const requestNumber = ++this.requestNumber
      this.trainingPage = null
      this.errorMessage = ''
      if (!Number.isInteger(this.trainingId) || this.trainingId <= 0) {
        this.errorMessage = this.$t('trainingView.loadError')
        return
      }
      TrainingService.sendGetTrainingSummaryRequest(
        this.trainingId,
        this.contentLang,
        this.requestedTrainingTranslationId || undefined,
      )
        .then((response) => {
          if (requestNumber === this.requestNumber) this.trainingPage = response.data
        })
        .catch(() => {
          if (requestNumber === this.requestNumber)
            this.errorMessage = this.$t('trainingView.loadError')
        })
    },
    formatDateRange(startDate, endDate) {
      return FormatService.formatDateRange(startDate, endDate)
    },
    attendanceText(course) {
      const attendances = []
      if (course.isOnSite) attendances.push(this.$t('courses.onSite'))
      if (course.isOnline) attendances.push(this.$t('courses.online'))
      return attendances.length === 0 ? '—' : attendances.join(' + ')
    },
  },
  beforeMount() {
    this.loadView()
  },
  beforeUnmount() {
    this.requestNumber++
  },
}
</script>

<template>
  <div class="flex flex-1 flex-col">
    <div v-if="trainingPage" class="border-b border-line bg-white">
      <div class="mx-auto flex w-full max-w-6xl flex-col px-4 pt-4 pb-8 sm:px-6 sm:pb-10">
        <BackLink :fallback="{ name: 'trainingsRoute' }" />
        <p v-if="trainingPage.isMainLanguageFallback" class="mb-2 text-sm text-muted">
          {{ $t('trainingView.mainLanguageFallback') }}
        </p>
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <span
            v-if="trainingPage.categoryName"
            class="rounded-md bg-brand-100 px-2.5 py-0.5 text-sm font-semibold text-brand-700"
          >
            {{ trainingPage.categoryName }}
          </span>
          <span v-if="trainingPage.isPromoted" class="badge text-bg-primary">
            <PhStar :size="13" weight="fill" />
            {{ $t('courses.promoted') }}
          </span>
        </div>
        <div class="flex items-start justify-between gap-3">
          <h1 class="text-3xl leading-tight font-extrabold tracking-tight sm:text-5xl">
            {{ trainingPage.title }}
          </h1>
          <EditTrainingLink
            :training-id="trainingId"
            :training-translation-id="trainingPage.trainingTranslationId"
            class="shrink-0"
          />
        </div>
        <p class="mt-3 max-w-3xl text-lg text-muted sm:text-xl">
          {{ trainingPage.shortDescription }}
        </p>
      </div>
    </div>

    <div class="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 sm:py-10">
      <BackLink v-if="!trainingPage" :fallback="{ name: 'trainingsRoute' }" />
      <AlertDanger :error-message="errorMessage" class="mb-6" />
      <p v-if="!trainingPage && !errorMessage" role="status" class="text-muted">
        {{ $t('trainingView.loading') }}
      </p>

      <div v-if="trainingPage" class="flex flex-col gap-6 lg:flex-row lg:items-start">
        <!-- Parem veerg on kitsal ekraanil esimene: andmed ja toimumiskorrad kohe nähtaval -->
        <aside class="flex flex-col gap-6 lg:order-2 lg:w-96 lg:shrink-0">
          <section class="rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">{{ $t('trainingView.sidebar.trainingData') }}</h2>
            <dl class="grid grid-cols-2 items-center gap-x-4 gap-y-3 text-[15px]">
              <dt class="text-muted">{{ $t('trainingCard.language') }}</dt>
              <dd>
                <FlagIcon
                  :flag-icon-code="trainingPage.trainingLanguageFlagIconCode"
                  :title="trainingPage.trainingLanguageCode"
                  class="text-2xl"
                />
              </dd>
              <dt class="text-muted">{{ $t('courses.filters.category') }}</dt>
              <dd class="font-semibold">
                <span v-if="trainingPage.categoryName" class="badge text-bg-primary">{{
                  trainingPage.categoryName
                }}</span
                ><span v-else>—</span>
              </dd>
              <dt class="text-muted">{{ $t('courses.filters.fundingType') }}</dt>
              <dd class="font-semibold">{{ fundingTypeNames || '—' }}</dd>
              <dt class="text-muted">{{ $t('trainingView.location') }}</dt>
              <dd class="font-semibold">
                {{ trainingPage.locationName || '—'
                }}<span v-if="trainingPage.isOnline" class="badge text-bg-light ml-2">{{
                  $t('courses.online')
                }}</span>
              </dd>
              <dt class="text-muted">{{ $t('trainingView.orderable') }}</dt>
              <dd class="font-semibold">
                {{ trainingPage.isOrderable ? $t('trainingView.yes') : $t('trainingView.no') }}
              </dd>
            </dl>
          </section>

          <section class="rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-2 text-lg font-bold">{{ $t('trainingView.sidebar.upcomingCourses') }}</h2>
            <ul v-if="trainingPage.upcomingCourses.length" class="flex flex-col">
              <li
                v-for="course in trainingPage.upcomingCourses"
                :key="course.courseId"
                class="border-b border-line last:border-0"
              >
                <RouterLink
                  :to="{
                    name: 'courseRoute',
                    query: { courseId: course.courseId },
                  }"
                  class="-mx-2 flex min-h-11 items-center justify-between gap-3 rounded-lg px-2 py-3 text-ink hover:bg-brand-50 hover:text-ink"
                >
                  <span class="flex flex-col">
                    <span class="font-semibold">
                      {{ formatDateRange(course.startDate, course.endDate) }}
                    </span>
                    <span class="text-sm text-muted">{{ attendanceText(course) }}</span>
                  </span>
                  <span v-if="course.status === 'F'" class="badge text-bg-warning">
                    {{ $t('courseStatus.F') }}
                  </span>
                  <PhCaretRight v-else :size="18" class="text-brand-600" />
                </RouterLink>
              </li>
            </ul>
            <p v-else class="text-sm text-muted">{{ $t('trainingView.noUpcomingCourses') }}</p>
            <RouterLink
              :to="{ name: 'coursesRoute' }"
              class="mt-3 inline-flex min-h-11 items-center gap-1 font-semibold"
            >
              {{ $t('trainingView.allCourses') }} →
            </RouterLink>
          </section>

          <section
            v-if="hasVisibleLecturers"
            class="rounded-2xl border border-line bg-white p-5 sm:p-6"
          >
            <h2 class="mb-3 text-lg font-bold">{{ $t('trainingView.sidebar.lecturers') }}</h2>
            <div class="flex flex-col gap-3">
              <LecturerCard
                v-for="lecturer in trainingPage.lecturers"
                :key="lecturer.lecturerId"
                :lecturer-summary="lecturer"
              />
            </div>
          </section>
        </aside>

        <div class="flex min-w-0 flex-1 flex-col gap-6 lg:order-1">
          <article class="rounded-2xl border border-line bg-white p-5 sm:p-8">
            <h2 class="mb-4 text-xl font-bold">{{ $t('trainingView.legend') }}</h2>
            <RichTextContent :html="trainingPage.description" class="max-w-prose" />
          </article>
          <section
            v-if="trainingPage.curriculumFileName"
            class="rounded-2xl border border-line bg-white p-5 sm:p-6"
          >
            <h2 class="mb-3 text-lg font-bold">
              {{ $t('trainingForm.translation.curriculumField') }}
            </h2>
            <a
              :href="curriculumUrl"
              :download="trainingPage.curriculumFileName"
              class="inline-flex min-h-11 items-center gap-2 break-all font-semibold"
            >
              <PhDownloadSimple :size="20" class="shrink-0" />
              {{ trainingPage.curriculumFileName }}
            </a>
            <span v-if="curriculumSize" class="ml-2 text-muted">({{ curriculumSize }})</span>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>
