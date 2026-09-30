<script>
// Kompaktsed teated vormi nuppude kõrval — ilmuvad animatsiooniga sinna, kus kasutaja just klikkis.
// Eduteade hääbub ise, veateade jääb, kuni kasutaja selle sulgeb või vaade selle tühjendab.
const SUCCESS_MESSAGE_TIMEOUT_MS = 4000

export default {
  name: 'InlineAlerts',
  props: {
    successMessage: String,
    errorMessage: String,
  },
  emits: ['event-success-message-closed', 'event-error-message-closed'],
  data() {
    return {
      successTimeoutId: null,
    }
  },
  watch: {
    successMessage(newSuccessMessage) {
      clearTimeout(this.successTimeoutId)
      if (newSuccessMessage !== '') {
        this.successTimeoutId = setTimeout(
          () => this.$emit('event-success-message-closed'),
          SUCCESS_MESSAGE_TIMEOUT_MS,
        )
      }
    },
  },
  beforeUnmount() {
    clearTimeout(this.successTimeoutId)
  },
}
</script>

<template>
  <div class="d-flex flex-wrap gap-2">
    <Transition name="inline-alert">
      <div
        v-if="successMessage !== ''"
        class="alert alert-success alert-dismissible py-2 mb-0"
        role="status"
        aria-live="polite"
      >
        {{ successMessage }}
        <button
          @click="$emit('event-success-message-closed')"
          :aria-label="$t('common.close')"
          class="btn-close py-2"
          type="button"
        ></button>
      </div>
    </Transition>
    <Transition name="inline-alert">
      <div
        v-if="errorMessage !== ''"
        class="alert alert-danger alert-dismissible py-2 mb-0"
        role="alert"
      >
        {{ errorMessage }}
        <button
          @click="$emit('event-error-message-closed')"
          :aria-label="$t('common.close')"
          class="btn-close py-2"
          type="button"
        ></button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.inline-alert-enter-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}

.inline-alert-leave-active {
  transition: opacity 0.6s ease;
}

.inline-alert-enter-from {
  opacity: 0;
  transform: translateX(-12px);
}

.inline-alert-leave-to {
  opacity: 0;
}
</style>
