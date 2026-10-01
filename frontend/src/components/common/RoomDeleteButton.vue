<script>
import { PhTrash } from '@phosphor-icons/vue'
import RoomService from '@/api-services/RoomService.js'
import NavigationService from '@/services/NavigationService.js'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Ruumi kustutamise ikoon (soft delete): teeb pärast kinnitust DELETE kutse ise.
// Tulevaste toimumiskordadega ruumi kustutada ei saa (nupp keelatud, backend kontrollib sama).
export default {
  name: 'RoomDeleteButton',
  components: { PhTrash, ConfirmModal },
  props: {
    roomId: Number,
    roomName: String,
    upcomingCourseCount: {
      type: Number,
      default: 0,
    },
  },
  emits: ['event-room-deleted', 'event-delete-error'],
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
        ? this.$t('roomDeleteButton.hasUpcomingCourses')
        : this.$t('roomDeleteButton.label')
    },
  },
  methods: {
    deleteRoom() {
      this.isModalOpen = false
      this.isSending = true
      RoomService.sendDeleteRoomRequest(this.roomId)
        .then(() => this.$emit('event-room-deleted', this.roomId))
        .catch((error) => this.handleDeleteRoomError(error))
        .finally(() => (this.isSending = false))
    },

    // 403 ROOM_HAS_UPCOMING_COURSES (nt teises aknas lisati toimumiskord) → vaade näitab teadet
    handleDeleteRoomError(error) {
      const errorResponse = error.response?.data
      if (
        error.response?.status === 403 &&
        errorResponse?.errorCode === 'ROOM_HAS_UPCOMING_COURSES'
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
  <span :title="buttonTitle" class="inline-flex">
    <button
      @click="isModalOpen = true"
      :disabled="isSending || hasUpcomingCourses"
      :aria-label="buttonTitle"
      class="btn btn-sm btn-icon btn-outline-danger"
      type="button"
    >
      <PhTrash :size="20" />
    </button>
  </span>
  <ConfirmModal
    :is-open="isModalOpen"
    :title="$t('roomDeleteButton.modalTitle')"
    :message="$t('roomDeleteButton.modalMessage', { roomName: roomName })"
    :confirm-label="$t('roomDeleteButton.confirm')"
    @event-confirmed="deleteRoom"
    @event-modal-closed="isModalOpen = false"
  />
</template>
