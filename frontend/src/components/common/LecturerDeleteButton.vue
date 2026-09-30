<script>
import { PhTrash } from '@phosphor-icons/vue'
import LecturerService from '@/api-services/LecturerService.js'
import NavigationService from '@/services/NavigationService.js'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Koolitaja kustutamise ikoon (soft delete): teeb pärast kinnitust DELETE kutse ise.
// Tulevaste toimumiskordadega koolitajat kustutada ei saa (nupp keelatud, backend kontrollib sama).
export default {
  name: 'LecturerDeleteButton',
  components: { PhTrash, ConfirmModal },
  props: {
    lecturerId: Number,
    fullName: String,
    upcomingCourseCount: {
      type: Number,
      default: 0,
    },
  },
  emits: ['event-lecturer-deleted', 'event-delete-error'],
  data() {
    return {
      isModalOpen: false,
      isSending: false,
    }
  },
  computed: {
    hasUpcomingCourses() {
      return this.upcomingCourseCount > 0
    },

    buttonTitle() {
      return this.hasUpcomingCourses
        ? this.$t('lecturerDeleteButton.hasUpcomingCourses')
        : this.$t('lecturerDeleteButton.label')
    },
  },
  methods: {
    deleteLecturer() {
      this.isModalOpen = false
      this.isSending = true
      LecturerService.sendDeleteLecturerRequest(this.lecturerId)
        .then(() => this.$emit('event-lecturer-deleted', this.lecturerId))
        .catch((error) => this.handleDeleteLecturerError(error))
        .finally(() => (this.isSending = false))
    },

    // 403 LECTURER_HAS_UPCOMING_COURSES (nt teises aknas lisati toimumiskord) → vaade näitab teadet
    handleDeleteLecturerError(error) {
      const errorResponse = error.response?.data
      if (
        error.response?.status === 403 &&
        errorResponse?.errorCode === 'LECTURER_HAS_UPCOMING_COURSES'
      ) {
        this.$emit('event-delete-error', errorResponse.message)
      } else {
        NavigationService.navigateToErrorView()
      }
    },
  },
}
</script>

<template>
  <!-- Keelatud nupu tooltip ei tööta, seega title on ümbritseval span'il -->
  <span :title="buttonTitle" class="d-inline-flex">
    <button
      @click="isModalOpen = true"
      :disabled="isSending || hasUpcomingCourses"
      :aria-label="buttonTitle"
      class="btn btn-sm btn-outline-danger d-inline-flex"
      type="button"
    >
      <PhTrash :size="20" />
    </button>
  </span>
  <ConfirmModal
    :is-open="isModalOpen"
    :title="$t('lecturerDeleteButton.modalTitle')"
    :message="$t('lecturerDeleteButton.modalMessage', { fullName: fullName })"
    :confirm-label="$t('lecturerDeleteButton.confirm')"
    @event-confirmed="deleteLecturer"
    @event-modal-closed="isModalOpen = false"
  />
</template>
