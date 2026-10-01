<script>
import BackLink from '@/components/common/BackLink.vue'
import { mapState } from 'pinia'
import { PhCheckCircle, PhPencilSimple } from '@phosphor-icons/vue'
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
  <div class="container">
    <BackLink :fallback="{ name: 'coursesRoute' }" />
    <AlertSuccess :success-message="successMessage" />

    <div v-if="coursePage" class="row text-start">
      <!-- Vasak veerg: koolituse sisu -->
      <div class="col-lg-8">
        <fieldset class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('trainingView.legend') }}</legend>

          <p v-if="coursePage.isMainLanguageFallback" class="small text-muted">
            {{ $t('trainingView.mainLanguageFallback') }}
          </p>

          <div class="d-flex justify-content-between align-items-center gap-2 mb-1">
            <div class="fs-4 fw-semibold">{{ coursePage.title }}</div>
            <RouterLink
              v-if="userIsAdmin"
              :to="{
                name: 'courseFormRoute',
                query: { returnTo: $route.fullPath, courseId: coursePage.courseId },
              }"
              :title="$t('courses.editCourse')"
              :aria-label="$t('courses.editCourse')"
              class="btn btn-sm btn-outline-secondary d-inline-flex"
            >
              <PhPencilSimple :size="20" />
            </RouterLink>
          </div>
          <div class="d-flex flex-wrap align-items-center gap-2 text-secondary mb-3">
            {{ formatDateRange(coursePage.startDate, coursePage.endDate) }}
            <span v-if="coursePage.isPast" class="badge text-bg-light border">
              {{ $t('courseStatus.past') }}
            </span>
          </div>
          <p class="fw-semibold">{{ coursePage.shortDescription }}</p>
          <RichTextContent :html="coursePage.description" />
          <RouterLink
            :to="{
              name: 'trainingRoute',
              query: {
                returnTo: $route.fullPath,
                trainingId: coursePage.trainingId,
                trainingTranslationId: coursePage.trainingTranslationId,
              },
            }"
          >
            {{ $t('courseView.allTrainingCourses') }}
          </RouterLink>
        </fieldset>
      </div>

      <!-- Parem veerg: toimumiskorrad, andmed, tegevused, koolitajad -->
      <div class="col-lg-4">
        <fieldset v-if="hasOtherCourses" class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('courseView.courses') }}</legend>
          <ul class="list-unstyled mb-0">
            <li
              v-for="upcomingCourse in coursePage.upcomingCourses"
              :key="upcomingCourse.courseId"
              class="mb-1"
            >
              <strong v-if="upcomingCourse.courseId === courseId">
                ▸ {{ formatDateRange(upcomingCourse.startDate, upcomingCourse.endDate) }} ·
                {{ attendanceText(upcomingCourse) }}
              </strong>
              <RouterLink
                v-else
                :to="{
                  name: 'courseRoute',
                  query: { courseId: upcomingCourse.courseId },
                }"
                replace
              >
                {{ formatDateRange(upcomingCourse.startDate, upcomingCourse.endDate) }} ·
                {{ attendanceText(upcomingCourse) }}
              </RouterLink>
              <span v-if="upcomingCourse.status === 'F'" class="badge text-bg-warning ms-2">
                {{ $t('courseStatus.F') }}
              </span>
            </li>
          </ul>
        </fieldset>

        <fieldset class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('courseView.course') }}</legend>
          <dl class="mb-3">
            <dt>{{ $t('adminCourse.fields.dates') }}</dt>
            <dd>{{ formatDateRange(coursePage.startDate, coursePage.endDate) }}</dd>
            <dt>{{ $t('adminTrainingCourses.columns.numberOfDays') }}</dt>
            <dd>{{ coursePage.numberOfDays }}</dd>
            <dt>{{ $t('adminTrainingCourses.columns.numberOfAcademicHours') }}</dt>
            <dd>{{ coursePage.numberOfAcademicHours }}</dd>
            <dt>{{ $t('adminTrainingCourses.columns.price') }}</dt>
            <dd>{{ formatPrice(coursePage.price) }}</dd>
            <dt>{{ $t('courses.filters.attendance') }}</dt>
            <dd>{{ attendanceText(coursePage) }}</dd>
            <dt>{{ $t('trainingCard.language') }}</dt>
            <dd>
              <FlagIcon :flag-icon-code="coursePage.trainingLanguageFlagIconCode" class="fs-5" />
            </dd>
            <dt>{{ $t('courses.filters.category') }}</dt>
            <dd>{{ coursePage.categoryName ?? '—' }}</dd>
            <dt>{{ $t('courses.filters.fundingType') }}</dt>
            <dd>{{ fundingTypeNames || '—' }}</dd>
          </dl>
          <span v-if="isFull" class="badge text-bg-warning mb-3">{{ $t('courseStatus.F') }}</span>

          <div v-if="!userIsAdmin" class="d-grid gap-2">
            <div
              v-if="isRegistered"
              class="alert alert-success d-flex align-items-center gap-2 mb-0"
            >
              <PhCheckCircle :size="20" />
              {{ $t('courseView.registered') }}
            </div>
            <button
              v-else
              @click="handleRegisterClick"
              :disabled="coursePage.isPast || isFull"
              class="btn btn-success"
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
        </fieldset>

        <fieldset v-if="hasVisibleLecturers" class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('trainingView.sidebar.lecturers') }}
          </legend>
          <LecturerCard
            v-for="lecturer in coursePage.lecturers"
            :key="lecturer.lecturerId"
            :lecturer-summary="lecturer"
            class="lecturer-card"
          />
        </fieldset>
      </div>

      <EnquiryModal
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

<style scoped>
/* Mitme koolitaja kaardid üksteise all */
.lecturer-card + .lecturer-card {
  margin-top: 1rem;
}
</style>
