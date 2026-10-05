<script>
import BackLink from '@/components/common/BackLink.vue'
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import CourseService from '@/api-services/CourseService.js'
import UserService from '@/api-services/UserService.js'
import FormatService from '@/services/FormatService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'
import AlertSuccess from '@/components/common/AlertSuccess.vue'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Kasutaja registreerib iseennast: /course-registration?courseId={id}
// (router guard: sisse logimata → login, admin → NotAuthorizedView)
export default {
  name: 'CourseRegistrationView',
  components: { BackLink, AlertDanger, AlertSuccess },
  data() {
    return {
      courseId: 0,
      coursePage: null,
      // R = registreerunud, C = loobunud, null = pole registreerunud
      participantStatus: null,
      registration: {
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        requiresLaptop: false,
        notes: '',
      },
      errorMessage: '',
      isSending: false,
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    backDestination() {
      return (
        NavigationService.getReturnTo(this.$route.query.returnTo, this.$route.fullPath) || {
          name: 'courseRoute',
          query: { courseId: this.courseId },
        }
      )
    },

    isRegistered() {
      return this.participantStatus === 'R'
    },

    isFull() {
      return this.coursePage !== null && this.coursePage.status === 'F'
    },

    lecturerNames() {
      return this.coursePage.lecturers.map((lecturer) => lecturer.fullName).join(', ')
    },

    attendanceText() {
      const attendances = []
      if (this.coursePage.isOnSite) {
        attendances.push(this.$t('courses.onSite'))
      }
      if (this.coursePage.isOnline) {
        attendances.push(this.$t('courses.online'))
      }
      return attendances.length === 0 ? '—' : attendances.join(' + ')
    },
  },
  watch: {
    contentLang() {
      this.getCoursePage()
    },
  },
  methods: {
    loadView() {
      this.courseId = Number(this.$route.query.courseId ?? 0)
      if (this.courseId === 0) {
        NavigationService.navigateToErrorView()
        return
      }
      this.getCoursePage()
      this.getParticipantStatus()
      this.getMyParticipant()
    },

    getCoursePage() {
      CourseService.sendGetCourseSummaryRequest(this.courseId, this.contentLang)
        .then((response) => (this.coursePage = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getParticipantStatus() {
      CourseService.sendGetCourseParticipantStatusRequest(
        this.courseId,
        SessionStorageService.getUserId(),
      )
        .then((response) => (this.participantStatus = response.data.status))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Vorm eeltäidetakse kasutaja osaleja profiilist
    getMyParticipant() {
      UserService.sendGetMyParticipantRequest(SessionStorageService.getUserId())
        .then((response) => this.handleGetMyParticipantResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetMyParticipantResponse(myParticipant) {
      this.registration.firstName = myParticipant.firstName
      this.registration.lastName = myParticipant.lastName
      this.registration.email = myParticipant.email
      this.registration.phone = myParticipant.phone
    },

    // ---------- "Registreeru" ----------

    registerCourseParticipant() {
      this.errorMessage = ''
      this.checkRegistrationForErrors()
      if (this.errorMessage !== '') {
        return
      }
      this.isSending = true
      CourseService.sendPostCourseParticipantRequest(this.courseId, {
        userId: SessionStorageService.getUserId(),
        firstName: this.registration.firstName.trim(),
        lastName: this.registration.lastName.trim(),
        email: this.registration.email.trim(),
        phone: this.registration.phone.trim(),
        requiresLaptop: this.registration.requiresLaptop,
        notes: this.registration.notes.trim(),
      })
        .then(() =>
          NavigationService.navigateBack(
            { name: 'courseRoute', query: { courseId: this.courseId } },
            this.$t('courseRegistration.messages.registered'),
          ),
        )
        .catch((error) => this.handleRegistrationError(error))
        .finally(() => (this.isSending = false))
    },

    // 403 (COURSE_FULL, ALREADY_REGISTERED, REGISTRATION_CLOSED) ja 400 → backendi teade vormis
    handleRegistrationError(error) {
      const statusCode = error.response?.status
      if (statusCode === 400 || statusCode === 403) {
        this.errorMessage = error.response.data?.message ?? ''
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    checkRegistrationForErrors() {
      const requiredValues = [
        this.registration.firstName,
        this.registration.lastName,
        this.registration.email,
        this.registration.phone,
      ]
      if (requiredValues.some((value) => value.trim() === '')) {
        this.errorMessage = this.$t('enquiryModal.validation.fillRequired')
      } else if (!EMAIL_PATTERN.test(this.registration.email.trim())) {
        this.errorMessage = this.$t('enquiryModal.validation.invalidEmail')
      }
    },

    navigateToCourseView() {
      NavigationService.navigateBack({ name: 'courseRoute', query: { courseId: this.courseId } })
    },

    formatDateRange(startDate, endDate) {
      return FormatService.formatDateRange(startDate, endDate)
    },

    formatPrice(price) {
      return FormatService.formatPrice(price, this.contentLang)
    },
  },
  beforeMount() {
    this.loadView()
  },
}
</script>

<template>
  <div class="container">
    <BackLink :fallback="{ name: 'courseRoute', query: { courseId } }" />
    <h1 class="h3 mb-3">{{ $t('courseRegistration.title') }}</h1>

    <div v-if="coursePage" class="row text-start">
      <div class="col-lg-5">
        <fieldset class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('courseView.course') }}</legend>
          <dl class="mb-3">
            <dt>{{ $t('adminCourse.fields.training') }}</dt>
            <dd>{{ coursePage.title }}</dd>
            <dt>{{ $t('adminCourse.fields.dates') }}</dt>
            <dd>{{ formatDateRange(coursePage.startDate, coursePage.endDate) }}</dd>
            <dt>{{ $t('courseRegistration.duration') }}</dt>
            <dd>
              {{
                $t('courses.duration', {
                  days: coursePage.numberOfDays,
                  hours: coursePage.numberOfAcademicHours,
                })
              }}
            </dd>
            <dt>{{ $t('adminTrainingCourses.columns.price') }}</dt>
            <dd>{{ formatPrice(coursePage.price) }}</dd>
            <dt>{{ $t('courses.filters.attendance') }}</dt>
            <dd>{{ attendanceText }}</dd>
            <dt>{{ $t('adminTrainingCourses.columns.lecturers') }}</dt>
            <dd>{{ lecturerNames || '—' }}</dd>
          </dl>
          <RouterLink :to="backDestination">
            {{ $t('courseRegistration.backToCourse') }}
          </RouterLink>
        </fieldset>
      </div>

      <div class="col-lg-7">
        <AlertSuccess
          v-if="isRegistered"
          :success-message="$t('courseRegistration.alreadyRegistered')"
        />
        <AlertDanger v-else-if="isFull" :error-message="$t('courseRegistration.full')" />
        <fieldset v-else class="border rounded bg-body p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('courseRegistration.participantData') }}
          </legend>
          <div class="row g-3">
            <div class="col-sm-6">
              <label class="form-label" for="registration-first-name">
                {{ $t('enquiryModal.firstName') }} *
              </label>
              <input
                v-model="registration.firstName"
                id="registration-first-name"
                class="form-control"
                type="text"
                maxlength="255"
              />
            </div>
            <div class="col-sm-6">
              <label class="form-label" for="registration-last-name">
                {{ $t('enquiryModal.lastName') }} *
              </label>
              <input
                v-model="registration.lastName"
                id="registration-last-name"
                class="form-control"
                type="text"
                maxlength="255"
              />
            </div>
            <div class="col-sm-6">
              <label class="form-label" for="registration-email">
                {{ $t('enquiryModal.email') }} *
              </label>
              <input
                v-model="registration.email"
                id="registration-email"
                class="form-control"
                type="email"
                maxlength="255"
              />
            </div>
            <div class="col-sm-6">
              <label class="form-label" for="registration-phone">
                {{ $t('enquiryModal.phone') }} *
              </label>
              <input
                v-model="registration.phone"
                id="registration-phone"
                class="form-control"
                type="tel"
                maxlength="20"
              />
            </div>
            <div class="col-12 form-text mt-1">{{ $t('courseRegistration.profileHint') }}</div>
            <div class="col-12">
              <div class="form-check">
                <input
                  v-model="registration.requiresLaptop"
                  id="registration-requires-laptop"
                  class="form-check-input"
                  type="checkbox"
                />
                <label class="form-check-label" for="registration-requires-laptop">
                  {{ $t('courseRegistration.requiresLaptop') }}
                </label>
              </div>
            </div>
            <div class="col-12">
              <label class="form-label" for="registration-notes">
                {{ $t('courseRegistration.notes') }}
              </label>
              <textarea
                v-model="registration.notes"
                id="registration-notes"
                class="form-control"
                rows="3"
              ></textarea>
            </div>
          </div>

          <AlertDanger :error-message="errorMessage" class="mt-3" />

          <div class="d-flex flex-wrap gap-2 mt-3">
            <button
              @click="registerCourseParticipant"
              :disabled="isSending"
              class="btn btn-success"
              type="button"
            >
              {{ $t('courseView.register') }}
            </button>
            <button @click="navigateToCourseView" class="btn btn-outline-secondary" type="button">
              {{ $t('courseRegistration.cancel') }}
            </button>
          </div>
        </fieldset>
      </div>
    </div>
  </div>
</template>
