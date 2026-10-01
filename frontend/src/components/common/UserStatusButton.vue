<script>
import UserService from '@/api-services/UserService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

const USER_STATUS_ACTIVE = 'A'

// Konto deaktiveerimine (aktiivne) või taastamine (deaktiveeritud), mõlemad kinnitusega:
// DELETE /api/admin-user/{userId}?currentUserId= / PUT /api/admin-user/{userId}/restore
export default {
  name: 'UserStatusButton',
  components: { ConfirmModal },
  props: {
    userId: Number,
    // Kinnituses näidatav nimi, nt "Anna Saar (kasutaja@vali-it.ee)"
    userLabel: String,
    status: String,
    deactivateLabel: String,
    restoreLabel: String,
  },
  emits: ['event-user-deactivated', 'event-user-restored', 'event-status-error'],
  data() {
    return {
      isModalOpen: false,
      isSending: false,
    }
  },
  computed: {
    isActive() {
      return this.status === USER_STATUS_ACTIVE
    },
  },
  methods: {
    changeUserStatus() {
      this.isModalOpen = false
      this.isSending = true
      const request = this.isActive
        ? UserService.sendDeleteAdminUserRequest(this.userId, SessionStorageService.getUserId())
        : UserService.sendPutAdminUserRestoreRequest(this.userId)
      request
        .then(() =>
          this.$emit(this.isActive ? 'event-user-deactivated' : 'event-user-restored', this.userId),
        )
        .catch((error) => this.handleChangeUserStatusError(error))
        .finally(() => (this.isSending = false))
    },

    // 403 CANNOT_DEACTIVATE_SELF → backendi teade vaatele
    handleChangeUserStatusError(error) {
      if (error.response?.status === 403) {
        this.$emit('event-status-error', error.response.data?.message ?? '')
      } else {
        NavigationService.navigateToErrorView()
      }
    },
  },
}
</script>

<template>
  <button
    @click="isModalOpen = true"
    :disabled="isSending"
    :class="isActive ? 'btn-outline-danger' : 'btn-outline-primary'"
    class="btn btn-sm"
    type="button"
  >
    {{
      isActive
        ? (deactivateLabel ?? $t('userStatusButton.deactivate'))
        : (restoreLabel ?? $t('userStatusButton.restore'))
    }}
  </button>
  <ConfirmModal
    :is-open="isModalOpen"
    :title="isActive ? $t('userStatusButton.deactivateTitle') : $t('userStatusButton.restoreTitle')"
    :message="
      isActive
        ? $t('userStatusButton.deactivateMessage', { name: userLabel })
        : $t('userStatusButton.restoreMessage', { name: userLabel })
    "
    :confirm-label="isActive ? $t('userStatusButton.deactivate') : $t('userStatusButton.restore')"
    @event-confirmed="changeUserStatus"
    @event-modal-closed="isModalOpen = false"
  />
</template>
