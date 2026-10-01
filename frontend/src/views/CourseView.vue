<script>
import BackLink from '@/components/common/BackLink.vue'
import { mapState } from 'pinia'
import { PhCalendarBlank, PhCaretRight, PhCheckCircle, PhPencilSimple } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import CourseService from '@/api-services/CourseService.js'
import FormatService from '@/services/FormatService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import AlertSuccess from '@/components/common/AlertSuccess.vue'
import FlagIcon from '@/components/common/FlagIcon.vue'
import LecturerCard from '@/components/common/LecturerCard.vue'
import RichTextContent from '@/components/common/RichTextContent.vue'
import EnquiryModal from '@/components/modals/EnquiryModal.vue'

// Avalik toimumiskorra leht: /course?courseId={id}
export default {
  name: 'CourseView',
  components: {
    BackLink,
    PhCalendarBlank,
    PhCaretRight,
    PhCheckCircle,
    PhPencilSimple,
    AlertSuccess,
    FlagIcon,
    LecturerCard,
    RichTextContent,
    EnquiryModal,
  },
  data() {
    return {
      courseId: 0,
      coursePage: null,
      // R = registreerunud, C = loobunud, null = pole registreerunud / pole sisse logitud
      participantStatus: null,
      successMessage: '',
      isEnquiryModalOpen: false,
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    userIsAdmin() {
      return SessionStorageService.userIsAdmin()
    },

    isRegistered() {
      return this.participantStatus === 'R'
    },

    isFull() {
      return this.coursePage.status === 'F'
    },

    // Teised toimumiskorrad on lingid; sektsioon peidetakse, kui peale praeguse teisi pole
    hasOtherCourses() {
      return this.coursePage.upcomingCourses.some(
        (upcomingCourse) => upcomingCourse.courseId !== this.courseId,
      )
    },

    hasVisibleLecturers() {
      return this.coursePage.lecturers.length > 0
    },

    fundingTypeNames() {
      return this.coursePage.fundingTypes
        .map((fundingType) => fundingType.fundingTypeName)
        .join(', ')
    },
  },
  watch: {
    // Teise toimumiskorra link → sama vaade uue courseId-ga
    '$route.query'() {
      this.loadView()
    },

    contentLang() {
      this.getCoursePage()
    },
  },
  methods: {
    loadView() {
      this.courseId = Number(this.$route.query.courseId ?? 0)
      this.successMessage = window.history.state?.successMessage ?? ''
      this.participantStatus = null
      if (this.courseId === 0) {
        NavigationService.navigateToErrorView()
        return
      }
      this.getCoursePage()
      this.getParticipantStatus()
    },

    getCoursePage() {
      CourseService.sendGetCourseSummaryRequest(this.courseId, this.contentLang)
        .then((response) => (this.coursePage = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Ainult sisseloginud mitte-admin kasutajale; olek on lisainfo — vea korral jääb nupp
    getParticipantStatus() {
      if (!SessionStorageService.userIsLoggedIn() || this.userIsAdmin) {
        return
      }
      CourseService.sendGetCourseParticipantStatusRequest(
        this.courseId,
        SessionStorageService.getUserId(),
      )
        .then((response) => (this.participantStatus = response.data.status))
        .catch(() => (this.participantStatus = null))
    },

    handleRegisterClick() {
      if (SessionStorageService.userIsLoggedIn()) {
        NavigationService.navigateToCourseRegistrationView(this.courseId)
      } else {
        const registrationLocation = this.$router.resolve({
          name: 'courseRegistrationRoute',
          query: NavigationService.withReturnTo({ courseId: this.courseId }),
        })
        NavigationService.navigateToLoginView(registrationLocation.fullPath)
      }
    },

    handleEnquirySent() {
      this.isEnquiryModalOpen = false
      this.successMessage = this.$t('courseView.messages.enquirySent')
    },

    formatDateRange(startDate, endDate) {
      return FormatService.formatDateRange(startDate, endDate)
    },

    formatPrice(price) {
      return FormatService.formatPrice(price, this.contentLang)
    },

    attendanceText(course) {
      const attendances = []
      if (course.isOnSite) {
        attendances.push(this.$t('courses.onSite'))
      }
      if (course.isOnline) {
        attendances.push(this.$t('courses.online'))
      }
      return attendances.length === 0 ? '—' : attendances.join(' + ')
    },
  },
  beforeMount() {
    this.loadView()
  },
}
</script>

<template>
  <div class="flex flex-1 flex-col">
    <div v-if="coursePage" class="border-b border-line bg-white">
      <div class="mx-auto flex w-full max-w-6xl flex-col px-4 pt-4 pb-8 sm:px-6 sm:pb-10">
        <BackLink :fallback="{ name: 'coursesRoute' }" />
        <p v-if="coursePage.isMainLanguageFallback" class="mb-2 text-sm text-muted">
          {{ $t('trainingView.mainLanguageFallback') }}
        </p>
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <span
            v-if="coursePage.categoryName"
            class="rounded-md bg-brand-100 px-2.5 py-0.5 text-sm font-semibold text-brand-700"
          >
            {{ coursePage.categoryName }}
          </span>
          <span v-if="isFull" class="badge text-bg-warning">{{ $t('courseStatus.F') }}</span>
          <span v-if="coursePage.isPast" class="badge text-bg-light">
            {{ $t('courseStatus.past') }}
          </span>
        </div>
        <div class="flex items-start justify-between gap-3">
          <h1 class="text-3xl leading-tight font-extrabold tracking-tight sm:text-5xl">
            {{ coursePage.title }}
          </h1>
          <RouterLink
            v-if="userIsAdmin"
            :to="{
              name: 'courseFormRoute',
              query: { returnTo: $route.fullPath, courseId: coursePage.courseId },
            }"
            :title="$t('courses.editCourse')"
            :aria-label="$t('courses.editCourse')"
            class="btn btn-outline-secondary btn-icon shrink-0"
          >
            <PhPencilSimple :size="20" />
          </RouterLink>
        </div>
        <p class="mt-3 max-w-3xl text-lg text-muted sm:text-xl">
          {{ coursePage.shortDescription }}
        </p>
        <p class="mt-4 inline-flex items-center gap-2 font-semibold text-navy">
          <PhCalendarBlank :size="20" class="text-brand-600" />
          {{ formatDateRange(coursePage.startDate, coursePage.endDate) }}
        </p>
      </div>
    </div>

    <div class="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 sm:py-10">
      <BackLink v-if="!coursePage" :fallback="{ name: 'coursesRoute' }" />
      <AlertSuccess :success-message="successMessage" class="mb-6" />

      <div v-if="coursePage" class="flex flex-col gap-6 lg:flex-row lg:items-start">
        <!-- Parem veerg on kitsal ekraanil esimene: andmed ja registreerumine kohe nähtaval -->
        <aside class="flex flex-col gap-6 lg:order-2 lg:w-96 lg:shrink-0">
          <section class="rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">{{ $t('courseView.course') }}</h2>
            <div class="mb-5 flex items-baseline justify-between gap-3">
              <span class="font-display text-3xl font-extrabold text-navy">
                {{ formatPrice(coursePage.price) }} €
              </span>
              <FlagIcon
                :flag-icon-code="coursePage.trainingLanguageFlagIconCode"
                :title="$t('trainingCard.language')"
                class="text-2xl"
              />
            </div>
            <dl class="grid grid-cols-2 gap-x-4 gap-y-3 text-[15px]">
              <dt class="text-muted">{{ $t('adminCourse.fields.dates') }}</dt>
              <dd class="font-semibold">
                {{ formatDateRange(coursePage.startDate, coursePage.endDate) }}
              </dd>
              <dt class="text-muted">{{ $t('adminTrainingCourses.columns.numberOfDays') }}</dt>
              <dd class="font-semibold">{{ coursePage.numberOfDays }}</dd>
              <dt class="text-muted">
                {{ $t('adminTrainingCourses.columns.numberOfAcademicHours') }}
              </dt>
              <dd class="font-semibold">{{ coursePage.numberOfAcademicHours }}</dd>
              <dt class="text-muted">{{ $t('courses.filters.attendance') }}</dt>
              <dd class="font-semibold">{{ attendanceText(coursePage) }}</dd>
              <dt class="text-muted">{{ $t('courses.filters.fundingType') }}</dt>
              <dd class="font-semibold">{{ fundingTypeNames || '—' }}</dd>
            </dl>

            <div v-if="!userIsAdmin" class="mt-6 flex flex-col gap-2">
              <div v-if="isRegistered" class="alert alert-success flex items-center gap-2">
                <PhCheckCircle :size="20" />
                {{ $t('courseView.registered') }}
              </div>
              <button
                v-else
                @click="handleRegisterClick"
                :disabled="coursePage.isPast || isFull"
                class="btn btn-primary btn-lg"
                type="button"
              >
                {{ isFull ? $t('courseView.full') : $t('courseView.register') }}
              </button>
              <button
                @click="isEnquiryModalOpen = true"
                :disabled="coursePage.isPast"
                class="btn btn-outline-primary"
                type="button"
              >
                {{ $t('enquiryModal.title') }}
              </button>
            </div>
          </section>

          <section
            v-if="hasOtherCourses"
            class="rounded-2xl border border-line bg-white p-5 sm:p-6"
          >
            <h2 class="mb-2 text-lg font-bold">{{ $t('courseView.courses') }}</h2>
            <ul class="flex flex-col">
              <li
                v-for="upcomingCourse in coursePage.upcomingCourses"
                :key="upcomingCourse.courseId"
                class="border-b border-line last:border-0"
              >
                <div
                  v-if="upcomingCourse.courseId === courseId"
                  class="-mx-2 flex items-center justify-between gap-3 rounded-lg bg-brand-50 px-2 py-3"
                  aria-current="true"
                >
                  <span class="flex flex-col">
                    <span class="font-semibold text-navy">
                      {{ formatDateRange(upcomingCourse.startDate, upcomingCourse.endDate) }}
                    </span>
                    <span class="text-sm text-muted">{{ attendanceText(upcomingCourse) }}</span>
                  </span>
                  <span v-if="upcomingCourse.status === 'F'" class="badge text-bg-warning">
                    {{ $t('courseStatus.F') }}
                  </span>
                </div>
                <RouterLink
                  v-else
                  :to="{
                    name: 'courseRoute',
                    query: { courseId: upcomingCourse.courseId },
                  }"
                  replace
                  class="-mx-2 flex items-center justify-between gap-3 rounded-lg px-2 py-3 text-ink hover:bg-brand-50 hover:text-ink"
                >
                  <span class="flex flex-col">
                    <span class="font-semibold">
                      {{ formatDateRange(upcomingCourse.startDate, upcomingCourse.endDate) }}
                    </span>
                    <span class="text-sm text-muted">{{ attendanceText(upcomingCourse) }}</span>
                  </span>
                  <span v-if="upcomingCourse.status === 'F'" class="badge text-bg-warning">
                    {{ $t('courseStatus.F') }}
                  </span>
                  <PhCaretRight v-else :size="18" class="text-brand-600" />
                </RouterLink>
              </li>
            </ul>
          </section>

          <section
            v-if="hasVisibleLecturers"
            class="rounded-2xl border border-line bg-white p-5 sm:p-6"
          >
            <h2 class="mb-3 text-lg font-bold">{{ $t('trainingView.sidebar.lecturers') }}</h2>
            <div class="flex flex-col gap-3">
              <LecturerCard
                v-for="lecturer in coursePage.lecturers"
                :key="lecturer.lecturerId"
                :lecturer-summary="lecturer"
              />
            </div>
          </section>
        </aside>

        <article
          class="min-w-0 flex-1 rounded-2xl border border-line bg-white p-5 sm:p-8 lg:order-1"
        >
          <h2 class="mb-4 text-xl font-bold">{{ $t('trainingView.legend') }}</h2>
          <RichTextContent :html="coursePage.description" class="max-w-prose" />
          <RouterLink
            :to="{
              name: 'trainingRoute',
              query: {
                returnTo: $route.fullPath,
                trainingId: coursePage.trainingId,
                trainingTranslationId: coursePage.trainingTranslationId,
              },
            }"
            class="mt-4 inline-flex items-center gap-1 font-semibold"
          >
            {{ $t('courseView.allTrainingCourses') }} →
          </RouterLink>
        </article>
      </div>

      <EnquiryModal
        v-if="coursePage"
        :is-open="isEnquiryModalOpen"
        :training-id="coursePage.trainingId"
        :course-id="coursePage.courseId"
        :title="coursePage.title"
        :start-date="coursePage.startDate"
        :end-date="coursePage.endDate"
        @event-enquiry-sent="handleEnquirySent"
        @event-modal-closed="isEnquiryModalOpen = false"
      />
    </div>
  </div>
</template>
