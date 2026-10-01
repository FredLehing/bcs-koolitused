<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import CourseService from '@/api-services/CourseService.js'
import EnquiryService from '@/api-services/EnquiryService.js'
import FormatService from '@/services/FormatService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import CourseStatusBadge from '@/components/common/CourseStatusBadge.vue'
import CourseEnquiriesTable from '@/components/course/CourseEnquiriesTable.vue'
import CourseParticipantsTable from '@/components/course/CourseParticipantsTable.vue'

// Toimumiskorra ülevaade (admin), ainult lugemiseks: /admin-course?courseId={id}
export default {
  name: 'AdminCourseView',
  components: { CourseStatusBadge, CourseEnquiriesTable, CourseParticipantsTable },
  data() {
    return {
      courseId: 0,
      course: null,
      courseParticipants: [],
      courseEnquiries: [],
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    // Avalik leht on olemas ainult avatud või täis toimumiskorral
    isPublic() {
      return this.course !== null && (this.course.status === 'O' || this.course.status === 'F')
    },
  },
  watch: {
    '$route.query'() {
      this.loadView()
    },

    // Keele vahetus → koolituse nimi uues keeles
    contentLang() {
      this.getAdminCourse()
    },
  },
  methods: {
    loadView() {
      this.courseId = Number(this.$route.query.courseId ?? 0)
      if (this.courseId === 0) {
        NavigationService.navigateToErrorView()
        return
      }
      this.getAdminCourse()
      this.getCourseParticipants()
      this.getCourseEnquiries()
    },

    getAdminCourse() {
      CourseService.sendGetAdminCourseRequest(this.courseId, this.contentLang)
        .then((response) => (this.course = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getCourseParticipants() {
      CourseService.sendGetCourseParticipantsRequest(this.courseId)
        .then((response) => (this.courseParticipants = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getCourseEnquiries() {
      EnquiryService.sendGetCourseEnquiriesRequest(this.courseId)
        .then((response) => (this.courseEnquiries = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    formatDateRange(startDate, endDate) {
      return FormatService.formatDateRange(startDate, endDate)
    },

    formatPrice(price) {
      return FormatService.formatPrice(price, this.contentLang)
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.loadView()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
  },
}
</script>

<template>
  <div class="container">
    <template v-if="course">
      <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-1">
        <h1 class="h3 mb-0">{{ $t('adminCourse.title') }}</h1>
        <div class="d-flex flex-wrap gap-2">
          <RouterLink
            :to="{ name: 'courseFormRoute', query: { courseId: course.courseId } }"
            class="btn btn-primary"
          >
            {{ $t('adminTrainingCourses.edit') }}
          </RouterLink>
          <RouterLink
            :to="{ name: 'adminTrainingCoursesRoute', query: { trainingId: course.trainingId } }"
            class="btn btn-outline-secondary"
          >
            {{ $t('adminTrainingCourses.title') }}
          </RouterLink>
          <RouterLink :to="{ name: 'adminAllCoursesRoute' }" class="btn btn-outline-secondary">
            {{ $t('navbar.manageCourses') }}
          </RouterLink>
        </div>
      </div>
      <p class="fs-5 text-secondary mb-4">{{ course.trainingTitle }}</p>

      <fieldset class="border rounded p-3 mb-4">
        <legend class="float-none w-auto px-2 fs-5">{{ $t('adminCourse.legend') }}</legend>
        <dl class="row mb-0">
          <dt class="col-sm-4 col-lg-3">{{ $t('adminCourse.fields.training') }}</dt>
          <dd class="col-sm-8 col-lg-9">
            <RouterLink
              :to="{
                name: 'trainingRoute',
                query: {
                  trainingId: course.trainingId,
                  trainingTranslationId: course.trainingTranslationId,
                },
              }"
            >
              {{ course.trainingTitle }}
            </RouterLink>
          </dd>
          <dt class="col-sm-4 col-lg-3">{{ $t('adminCourse.fields.dates') }}</dt>
          <dd class="col-sm-8 col-lg-9">{{ formatDateRange(course.startDate, course.endDate) }}</dd>
          <dt class="col-sm-4 col-lg-3">{{ $t('adminTrainingCourses.columns.numberOfDays') }}</dt>
          <dd class="col-sm-8 col-lg-9">{{ course.numberOfDays }}</dd>
          <dt class="col-sm-4 col-lg-3">
            {{ $t('adminTrainingCourses.columns.numberOfAcademicHours') }}
          </dt>
          <dd class="col-sm-8 col-lg-9">{{ course.numberOfAcademicHours }}</dd>
          <dt class="col-sm-4 col-lg-3">{{ $t('adminTrainingCourses.columns.price') }}</dt>
          <dd class="col-sm-8 col-lg-9">{{ formatPrice(course.price) }}</dd>
          <dt class="col-sm-4 col-lg-3">{{ $t('adminTrainingCourses.columns.lecturers') }}</dt>
          <dd class="col-sm-8 col-lg-9">{{ course.lecturerNames ?? '—' }}</dd>
          <dt class="col-sm-4 col-lg-3">{{ $t('adminTrainingCourses.columns.room') }}</dt>
          <dd class="col-sm-8 col-lg-9">{{ course.roomName ?? '—' }}</dd>
          <dt class="col-sm-4 col-lg-3">{{ $t('adminTrainingCourses.columns.meetingLink') }}</dt>
          <dd class="col-sm-8 col-lg-9 text-break">
            <a v-if="course.meetingLink" :href="course.meetingLink" target="_blank" rel="noopener">
              {{ course.meetingLink }}
            </a>
            <template v-else>—</template>
          </dd>
          <dt class="col-sm-4 col-lg-3">{{ $t('adminTrainingCourses.columns.status') }}</dt>
          <dd class="col-sm-8 col-lg-9">
            <CourseStatusBadge :status="course.status" :is-past="course.isPast" />
          </dd>
          <dt class="col-sm-4 col-lg-3">{{ $t('courseForm.isPromoted') }}</dt>
          <dd class="col-sm-8 col-lg-9">
            {{
              course.isPromoted
                ? $t('adminAllCourses.filters.yes')
                : $t('adminAllCourses.filters.no')
            }}
          </dd>
          <dt class="col-sm-4 col-lg-3">{{ $t('adminTrainingCourses.columns.notes') }}</dt>
          <dd class="col-sm-8 col-lg-9 notes">{{ course.notes ?? '—' }}</dd>
        </dl>
        <RouterLink
          v-if="isPublic"
          :to="{ name: 'courseRoute', query: { courseId: course.courseId } }"
          class="d-inline-block mt-2"
        >
          {{ $t('adminCourse.viewPublicPage') }}
        </RouterLink>
      </fieldset>

      <CourseParticipantsTable
        :course-id="courseId"
        :course-participants="courseParticipants"
        class="mb-4"
      />

      <CourseEnquiriesTable :course-enquiries="courseEnquiries" class="mb-5" />
    </template>
  </div>
</template>

<style scoped>
.notes {
  white-space: pre-wrap;
}
</style>
