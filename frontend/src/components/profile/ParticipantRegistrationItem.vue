<script>
import FormatService from '@/services/FormatService.js'
import CourseParticipantStatusBadge from '@/components/common/CourseParticipantStatusBadge.vue'

// Avalikul toimumiskorra lehel (/course) on ainult avatud ja täis toimumiskorrad
const PUBLIC_COURSE_STATUSES = ['O', 'F']
const COURSE_STATUS_CANCELLED = 'X'

// "Minu koolitused" üks rida: koolitus, toimumisaeg, vorm, märgised, (canCancel korral) "Loobu"
// ja (canGiveFeedback korral) "Anna tagasisidet" / "Vaata tagasisidet"
export default {
  name: 'ParticipantRegistrationItem',
  components: { CourseParticipantStatusBadge },
  props: {
    registration: Object,
    isSending: Boolean,
  },
  emits: ['event-cancel-clicked'],
  computed: {
    isCoursePublic() {
      return PUBLIC_COURSE_STATUSES.includes(this.registration.courseStatus)
    },

    isCourseCancelled() {
      return this.registration.courseStatus === COURSE_STATUS_CANCELLED
    },

    dateRangeText() {
      return FormatService.formatDateRange(this.registration.startDate, this.registration.endDate)
    },

    attendanceText() {
      const attendances = []
      if (this.registration.isOnSite) {
        attendances.push(this.$t('courses.onSite'))
      }
      if (this.registration.isOnline) {
        attendances.push(this.$t('courses.online'))
      }
      return attendances.join(' + ')
    },
  },
}
</script>

<template>
  <li class="flex flex-col gap-3 p-4 sm:flex-row sm:items-center">
    <div class="min-w-0 flex-1">
      <RouterLink
        v-if="isCoursePublic"
        :to="{
          name: 'courseRoute',
          query: { returnTo: $route.fullPath, courseId: registration.courseId },
        }"
        class="font-semibold"
      >
        {{ registration.trainingTitle }}
      </RouterLink>
      <span v-else class="font-semibold text-navy">{{ registration.trainingTitle }}</span>
      <div class="text-sm text-muted">
        {{ dateRangeText }}
        <template v-if="attendanceText"> · {{ attendanceText }}</template>
      </div>
      <div class="mt-2 flex flex-wrap gap-1">
        <CourseParticipantStatusBadge :status="registration.status" />
        <span v-if="isCourseCancelled" class="badge text-bg-danger">
          {{ $t('courseStatus.X') }}
        </span>
        <span
          class="badge border"
          :class="registration.hasPaid ? 'text-bg-success' : 'text-bg-light'"
        >
          {{
            registration.hasPaid ? $t('participantCourses.paid') : $t('participantCourses.notPaid')
          }}
        </span>
      </div>
    </div>
    <RouterLink
      v-if="registration.canGiveFeedback"
      :to="{
        name: 'participantFeedbackFormRoute',
        query: { courseParticipantId: registration.courseParticipantId },
      }"
      class="btn w-full sm:w-auto"
      :class="registration.hasFeedback ? 'btn-outline-primary' : 'btn-primary'"
    >
      {{
        registration.hasFeedback
          ? $t('participantCourses.viewFeedback')
          : $t('participantCourses.giveFeedback')
      }}
    </RouterLink>
    <button
      v-if="registration.canCancel"
      @click="$emit('event-cancel-clicked', registration)"
      :disabled="isSending"
      class="btn btn-outline-danger w-full sm:w-auto"
      type="button"
    >
      {{ $t('participantCourses.cancel') }}
    </button>
  </li>
</template>
