<script>
import RoomService from '@/api-services/RoomService.js'
import NavigationService from '@/services/NavigationService.js'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Kustutatud ruumi taastamine: kinnitus → PUT /api/room/{roomId}/restore
export default {
  name: 'RoomRestoreButton',
  components: { ConfirmModal },
  props: {
    roomId: Number,
    roomName: String,
  },
  emits: ['event-room-restored'],
  data() {
    return {
      isModalOpen: false,
      isSending: false,
    }
  },
  methods: {
    restoreRoom() {
      this.isModalOpen = false
      this.isSending = true
      RoomService.sendPutRoomRestoreRequest(this.roomId)
        .then(() => this.$emit('event-room-restored', this.roomId))
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
    class="btn btn-sm btn-outline-primary"
    type="button"
  >
    {{ $t('roomRestoreButton.label') }}
  </button>
  <ConfirmModal
    :is-open="isModalOpen"
    :title="$t('roomRestoreButton.modalTitle')"
    :message="$t('roomRestoreButton.modalMessage', { roomName: roomName })"
    :confirm-label="$t('roomRestoreButton.confirm')"
    @event-confirmed="restoreRoom"
    @event-modal-closed="isModalOpen = false"
  />
</template>
