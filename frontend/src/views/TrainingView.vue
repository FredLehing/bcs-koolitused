<script>
import { mapState } from 'pinia'
import { PhDownloadSimple, PhStar } from '@phosphor-icons/vue'
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
  <div class="container">
    <BackLink :fallback="{ name: 'trainingsRoute' }" />
    <AlertDanger :error-message="errorMessage" />
    <p v-if="!trainingPage && !errorMessage" role="status">{{ $t('trainingView.loading') }}</p>
    <div v-if="trainingPage" class="row text-start">
      <div class="col-lg-8">
        <fieldset class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('trainingView.legend') }}</legend>
          <p v-if="trainingPage.isMainLanguageFallback" class="small text-muted">
            {{ $t('trainingView.mainLanguageFallback') }}
          </p>
          <h1 class="h4 d-flex align-items-center gap-2 mb-3">
            <PhStar
              v-if="trainingPage.isPromoted"
              :size="20"
              weight="fill"
              class="text-warning flex-shrink-0"
              :aria-label="$t('courses.promoted')"
            />
            {{ trainingPage.title }}
            <EditTrainingLink
              :training-id="trainingId"
              :training-translation-id="trainingPage.trainingTranslationId"
              class="flex-shrink-0"
            />
          </h1>
          <p class="fw-semibold">{{ trainingPage.shortDescription }}</p>
          <RichTextContent :html="trainingPage.description" />
        </fieldset>
        <fieldset v-if="trainingPage.curriculumFileName" class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('trainingForm.translation.curriculumField') }}
          </legend>
          <a
            :href="curriculumUrl"
            :download="trainingPage.curriculumFileName"
            class="d-inline-flex align-items-center gap-2 text-break"
          >
            <PhDownloadSimple :size="20" class="flex-shrink-0" />
            {{ trainingPage.curriculumFileName }}
          </a>
          <span v-if="curriculumSize" class="text-secondary ms-2">({{ curriculumSize }})</span>
        </fieldset>
      </div>
      <div class="col-lg-4">
        <fieldset class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('trainingView.sidebar.trainingData') }}
          </legend>
          <dl class="mb-0">
            <dt>{{ $t('trainingCard.language') }}</dt>
            <dd>
              <FlagIcon
                :flag-icon-code="trainingPage.trainingLanguageFlagIconCode"
                :title="trainingPage.trainingLanguageCode"
                class="fs-5"
              />
            </dd>
            <dt>{{ $t('courses.filters.category') }}</dt>
            <dd>
              <span v-if="trainingPage.categoryName" class="badge text-bg-primary">{{
                trainingPage.categoryName
              }}</span
              ><span v-else>—</span>
            </dd>
            <dt>{{ $t('courses.filters.fundingType') }}</dt>
            <dd>{{ fundingTypeNames || '—' }}</dd>
            <dt>{{ $t('trainingView.location') }}</dt>
            <dd>
              {{ trainingPage.locationName || '—'
              }}<span v-if="trainingPage.isOnline" class="badge text-bg-light border ms-2">{{
                $t('courses.online')
              }}</span>
            </dd>
            <dt>{{ $t('trainingView.orderable') }}</dt>
            <dd>{{ trainingPage.isOrderable ? $t('trainingView.yes') : $t('trainingView.no') }}</dd>
          </dl>
        </fieldset>
        <fieldset v-if="hasVisibleLecturers" class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('trainingView.sidebar.lecturers') }}
          </legend>
          <LecturerCard
            v-for="lecturer in trainingPage.lecturers"
            :key="lecturer.lecturerId"
            :lecturer-summary="lecturer"
            class="lecturer-card"
          />
        </fieldset>
        <fieldset class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('trainingView.sidebar.upcomingCourses') }}
          </legend>
          <ul v-if="trainingPage.upcomingCourses.length" class="list-unstyled mb-0">
            <li v-for="course in trainingPage.upcomingCourses" :key="course.courseId" class="mb-2">
              <RouterLink
                :to="{
                  name: 'courseRoute',
                  query: { courseId: course.courseId },
                }"
              >
                {{ formatDateRange(course.startDate, course.endDate) }} ·
                {{ attendanceText(course) }}
              </RouterLink>
              <span v-if="course.status === 'F'" class="badge text-bg-warning ms-2">{{
                $t('courseStatus.F')
              }}</span>
            </li>
          </ul>
          <p v-else class="small text-muted mb-0">{{ $t('trainingView.noUpcomingCourses') }}</p>
        </fieldset>
        <fieldset class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('trainingView.sidebar.calendar') }}
          </legend>
          <RouterLink :to="{ name: 'coursesRoute' }">{{
            $t('trainingView.allCourses')
          }}</RouterLink>
        </fieldset>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lecturer-card + .lecturer-card {
  margin-top: 1rem;
}
</style>
