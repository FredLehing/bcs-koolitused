<script>
import LecturerService from '@/api-services/LecturerService.js'
import NavigationService from '@/services/NavigationService.js'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Kustutatud koolitaja taastamine: kinnitus → PUT /api/lecturer/{lecturerId}/restore
export default {
  name: 'LecturerRestoreButton',
  components: { ConfirmModal },
  props: {
    lecturerId: Number,
    fullName: String,
  },
  emits: ['event-lecturer-restored'],
  data() {
    return {
      isModalOpen: false,
      isSending: false,
    }
  },
  methods: {
    restoreLecturer() {
      this.isModalOpen = false
      this.isSending = true
      LecturerService.sendPutLecturerRestoreRequest(this.lecturerId)
        .then(() => this.$emit('event-lecturer-restored', this.lecturerId))
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
    {{ $t('lecturerRestoreButton.label') }}
  </button>
  <ConfirmModal
    :is-open="isModalOpen"
    :title="$t('lecturerRestoreButton.modalTitle')"
    :message="$t('lecturerRestoreButton.modalMessage', { fullName: fullName })"
    :confirm-label="$t('lecturerRestoreButton.confirm')"
    @event-confirmed="restoreLecturer"
    @event-modal-closed="isModalOpen = false"
  />
</template>
