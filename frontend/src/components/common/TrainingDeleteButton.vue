<script>
import { PhTrash } from '@phosphor-icons/vue'
import TrainingService from '@/api-services/TrainingService.js'
import NavigationService from '@/services/NavigationService.js'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Koolituse kustutamise ikoon (soft delete): teeb pärast kinnitust DELETE kutse ise
// ja annab vaatele teada sündmusega event-training-deleted.
export default {
  name: 'TrainingDeleteButton',
  components: { PhTrash, ConfirmModal },
  props: {
    trainingId: Number,
    title: String,
  },
  emits: ['event-training-deleted'],
  data() {
    return {
      isModalOpen: false,
      isSending: false,
    }
  },
  methods: {
    deleteTraining() {
      this.isModalOpen = false
      this.isSending = true
      TrainingService.sendDeleteTrainingRequest(this.trainingId)
        .then(() => this.$emit('event-training-deleted', this.trainingId))
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
    :title="$t('trainingDeleteButton.label')"
    :aria-label="$t('trainingDeleteButton.label')"
    class="btn btn-sm btn-icon btn-outline-danger"
    type="button"
  >
    <PhTrash :size="20" />
  </button>
  <ConfirmModal
    :is-open="isModalOpen"
    :title="$t('trainingDeleteButton.modalTitle')"
    :message="$t('trainingDeleteButton.modalMessage', { title: title })"
    :confirm-label="$t('trainingDeleteButton.confirm')"
    @event-confirmed="deleteTraining"
    @event-modal-closed="isModalOpen = false"
  />
</template>
