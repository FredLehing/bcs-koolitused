<script>
import BackLink from '@/components/common/BackLink.vue'
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
  components: { BackLink, CourseStatusBadge, CourseEnquiriesTable, CourseParticipantsTable },
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
  <div class="mx-auto w-full max-w-7xl px-6 py-8">
    <BackLink
      :fallback="
        course
          ? { name: 'adminTrainingCoursesRoute', query: { trainingId: course.trainingId } }
          : { name: 'adminAllCoursesRoute' }
      "
    />
    <template v-if="course">
      <div class="mb-1 flex flex-wrap items-center justify-between gap-2">
        <h1 class="text-3xl font-extrabold tracking-tight">{{ $t('adminCourse.title') }}</h1>
        <div class="flex flex-wrap gap-2">
          <RouterLink
            :to="{
              name: 'courseFormRoute',
              query: { returnTo: $route.fullPath, courseId: course.courseId },
            }"
            class="btn btn-primary"
          >
            {{ $t('adminTrainingCourses.edit') }}
          </RouterLink>
          <RouterLink
            :to="{
              name: 'adminTrainingCoursesRoute',
              query: { returnTo: $route.fullPath, trainingId: course.trainingId },
            }"
            class="btn btn-outline-secondary"
          >
            {{ $t('adminTrainingCourses.title') }}
          </RouterLink>
          <RouterLink :to="{ name: 'adminAllCoursesRoute' }" class="btn btn-outline-secondary">
            {{ $t('navbar.manageCourses') }}
          </RouterLink>
        </div>
      </div>
      <p class="mb-6 text-lg text-muted">{{ course.trainingTitle }}</p>

      <section class="mb-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
        <h2 class="mb-4 text-lg font-bold">{{ $t('adminCourse.legend') }}</h2>
        <dl class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-[12rem_1fr]">
          <dt class="text-muted">{{ $t('adminCourse.fields.training') }}</dt>
          <dd>
            <RouterLink
              :to="{
                name: 'trainingRoute',
                query: {
                  returnTo: $route.fullPath,
                  trainingId: course.trainingId,
                  trainingTranslationId: course.trainingTranslationId,
                },
              }"
            >
              {{ course.trainingTitle }}
            </RouterLink>
          </dd>
          <dt class="text-muted">{{ $t('adminCourse.fields.dates') }}</dt>
          <dd>{{ formatDateRange(course.startDate, course.endDate) }}</dd>
          <dt class="text-muted">{{ $t('adminTrainingCourses.columns.numberOfDays') }}</dt>
          <dd>{{ course.numberOfDays }}</dd>
          <dt class="text-muted">
            {{ $t('adminTrainingCourses.columns.numberOfAcademicHours') }}
          </dt>
          <dd>{{ course.numberOfAcademicHours }}</dd>
          <dt class="text-muted">{{ $t('adminTrainingCourses.columns.price') }}</dt>
          <dd>{{ formatPrice(course.price) }}</dd>
          <dt class="text-muted">{{ $t('adminTrainingCourses.columns.lecturers') }}</dt>
          <dd>{{ course.lecturerNames ?? '—' }}</dd>
          <dt class="text-muted">{{ $t('adminTrainingCourses.columns.room') }}</dt>
          <dd>{{ course.roomName ?? '—' }}</dd>
          <dt class="text-muted">{{ $t('adminTrainingCourses.columns.meetingLink') }}</dt>
          <dd class="break-all">
            <a v-if="course.meetingLink" :href="course.meetingLink" target="_blank" rel="noopener">
              {{ course.meetingLink }}
            </a>
            <template v-else>—</template>
          </dd>
          <dt class="text-muted">{{ $t('adminTrainingCourses.columns.status') }}</dt>
          <dd>
            <CourseStatusBadge :status="course.status" :is-past="course.isPast" />
          </dd>
          <dt class="text-muted">{{ $t('courseForm.isPromoted') }}</dt>
          <dd>
            {{
              course.isPromoted
                ? $t('adminAllCourses.filters.yes')
                : $t('adminAllCourses.filters.no')
            }}
          </dd>
          <dt class="text-muted">{{ $t('adminTrainingCourses.columns.notes') }}</dt>
          <dd class="whitespace-pre-wrap">{{ course.notes ?? '—' }}</dd>
        </dl>
        <RouterLink
          v-if="isPublic"
          :to="{
            name: 'courseRoute',
            query: { returnTo: $route.fullPath, courseId: course.courseId },
          }"
          class="mt-4 inline-block font-semibold"
        >
          {{ $t('adminCourse.viewPublicPage') }}
        </RouterLink>
      </section>

      <CourseParticipantsTable
        :course-id="courseId"
        :course-participants="courseParticipants"
        class="mb-6"
      />

      <CourseEnquiriesTable :course-enquiries="courseEnquiries" class="mb-6" />
    </template>
  </div>
</template>
