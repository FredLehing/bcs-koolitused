<script>
import { PhTrash } from '@phosphor-icons/vue'
import CourseService from '@/api-services/CourseService.js'
import NavigationService from '@/services/NavigationService.js'
import FormatService from '@/services/FormatService.js'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Toimumiskorra kustutamise ikoon (soft delete): kinnitus → DELETE /api/course/{courseId}.
// Osalejate korral hoiatab modal ja soovitab tühistamist (kustutamist ei keela).
export default {
  name: 'CourseDeleteButton',
  components: { PhTrash, ConfirmModal },
  props: {
    courseId: Number,
    startDate: String,
    endDate: String,
    participantCount: {
      type: Number,
      default: 0,
    },
  },
  emits: ['event-course-deleted'],
  data() {
    return {
      isModalOpen: false,
      isSending: false,
    }
  },
  computed: {
    modalMessage() {
      const message = this.$t('courseDeleteButton.modalMessage', {
        dates: `${FormatService.formatLocalDate(this.startDate)} – ${FormatService.formatLocalDate(this.endDate)}`,
      })
      if (this.participantCount > 0) {
        return (
          message + ' ' + this.$t('courseDeleteButton.participantsWarning', this.participantCount)
        )
      }
      return message
    },
  },
  methods: {
    deleteCourse() {
      this.isModalOpen = false
      this.isSending = true
      CourseService.sendDeleteCourseRequest(this.courseId)
        .then(() => this.$emit('event-course-deleted', this.courseId))
        .catch(() => NavigationService.navigateToErrorView())
        .finally(() => (this.isSending = false))
    },
  },
}
</script>

<template>
  <button
    @click="isModalOpen = true"
    :disabled="isSending"
    :title="$t('courseDeleteButton.label')"
    :aria-label="$t('courseDeleteButton.label')"
    class="btn btn-sm btn-outline-danger d-inline-flex"
    type="button"
  >
    <PhTrash :size="20" />
  </button>
  <ConfirmModal
    :is-open="isModalOpen"
    :title="$t('courseDeleteButton.modalTitle')"
    :message="modalMessage"
    :confirm-label="$t('courseDeleteButton.confirm')"
    @event-confirmed="deleteCourse"
    @event-modal-closed="isModalOpen = false"
  />
</template>
