<script>
import { PhX } from '@phosphor-icons/vue'

// Kompaktsed teated vormi nuppude kõrval — ilmuvad animatsiooniga sinna, kus kasutaja just klikkis.
// Eduteade hääbub ise, veateade jääb, kuni kasutaja selle sulgeb või vaade selle tühjendab.
const SUCCESS_MESSAGE_TIMEOUT_MS = 4000

export default {
  name: 'InlineAlerts',
  components: { PhX },
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
  <div class="flex flex-wrap gap-2">
    <Transition name="inline-alert">
      <div
        v-if="successMessage !== ''"
        class="alert alert-success flex items-center gap-3 py-2"
        role="status"
        aria-live="polite"
      >
        {{ successMessage }}
        <button
          @click="$emit('event-success-message-closed')"
          :aria-label="$t('common.close')"
          class="cursor-pointer rounded p-1 opacity-70 hover:opacity-100"
          type="button"
        >
          <PhX :size="16" />
        </button>
      </div>
    </Transition>
    <Transition name="inline-alert">
      <div
        v-if="errorMessage !== ''"
        class="alert alert-danger flex items-center gap-3 py-2"
        role="alert"
      >
        {{ errorMessage }}
        <button
          @click="$emit('event-error-message-closed')"
          :aria-label="$t('common.close')"
          class="cursor-pointer rounded p-1 opacity-70 hover:opacity-100"
          type="button"
        >
          <PhX :size="16" />
        </button>
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
