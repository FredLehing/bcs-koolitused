<script>
import TrainingService from '@/api-services/TrainingService.js'
import NavigationService from '@/services/NavigationService.js'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Koolituse staatuse nupp: teeb pärast kinnitust API kutse ise ja annab tulemusest sündmusega teada.
//   U (mustand)        → "Publitseeri"          → PUT /api/training/{trainingId}/publish   → P
//   P (publitseeritud) → "Liiguta mustandisse"  → PUT /api/training/{trainingId}/unpublish → U
//   D (kustutatud)     → "Taasta"               → PUT /api/training/{trainingId}/restore   → U
const STATUS_ACTIONS = {
  U: { key: 'publish', newStatus: 'P', sendRequest: TrainingService.sendPutTrainingPublishRequest },
  P: {
    key: 'unpublish',
    newStatus: 'U',
    sendRequest: TrainingService.sendPutTrainingUnpublishRequest,
  },
  D: { key: 'restore', newStatus: 'U', sendRequest: TrainingService.sendPutTrainingRestoreRequest },
}

export default {
  name: 'TrainingStatusButton',
  components: { ConfirmModal },
  props: {
    trainingId: Number,
    status: String,
    // Kui antud, kuvatakse koolituse nimi kinnituse tekstis (tabelis); vormis jäetakse ära
    title: {
      type: String,
      default: '',
    },
    buttonClass: {
      type: String,
      default: 'btn btn-outline-primary',
    },
  },
  emits: ['event-status-changed', 'event-status-error'],
  data() {
    return {
      isModalOpen: false,
      isSending: false,
    }
  },
  computed: {
    statusAction() {
      return STATUS_ACTIONS[this.status]
    },

    buttonLabel() {
      return this.$t(`trainingStatusButton.buttons.${this.statusAction.key}`)
    },

    modalTitle() {
      return this.$t(`trainingStatusButton.modal.${this.statusAction.key}Title`)
    },

    modalMessage() {
      return this.title === ''
        ? this.$t(`trainingStatusButton.modal.${this.statusAction.key}Message`)
        : this.$t(`trainingStatusButton.modal.${this.statusAction.key}MessageWithTitle`, {
            title: this.title,
          })
    },
  },
  methods: {
    changeTrainingStatus() {
      this.isModalOpen = false
      this.isSending = true
      const newStatus = this.statusAction.newStatus
      this.statusAction
        .sendRequest(this.trainingId)
        .then(() => this.$emit('event-status-changed', newStatus))
        .catch((error) => this.handleChangeTrainingStatusError(error))
        .finally(() => (this.isSending = false))
    },

    // 403 TRAINING_DELETED: koolitus kustutati vahepeal (nt teises aknas) — vaade näitab teadet ja laadib andmed uuesti
    handleChangeTrainingStatusError(error) {
      const errorResponse = error.response?.data ?? { message: '', errorCode: '' }
      if (error.response?.status === 403 && errorResponse.errorCode === 'TRAINING_DELETED') {
        this.$emit('event-status-error', errorResponse.message)
      } else {
        NavigationService.navigateToErrorView()
      }
    },
  },
}
</script>

<template>
  <template v-if="statusAction">
    <button @click="isModalOpen = true" :class="buttonClass" :disabled="isSending" type="button">
      {{ buttonLabel }}
    </button>
    <ConfirmModal
      :is-open="isModalOpen"
      :title="modalTitle"
      :message="modalMessage"
      :confirm-label="buttonLabel"
      @event-confirmed="changeTrainingStatus"
      @event-modal-closed="isModalOpen = false"
    />
  </template>
</template>
