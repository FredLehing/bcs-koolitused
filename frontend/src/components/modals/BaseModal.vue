<script>
import { PhX } from '@phosphor-icons/vue'

export default {
  name: 'BaseModal',
  components: { PhX },
  props: {
    isOpen: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-modal-closed'],
  watch: {
    // Avatud modaali ajal leht taustal ei keri
    isOpen(isOpen) {
      document.body.style.overflow = isOpen ? 'hidden' : ''
    },
  },
  methods: {
    close() {
      this.$emit('event-modal-closed')
    },
  },
  beforeUnmount() {
    document.body.style.overflow = ''
  },
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="isOpen"
      class="fixed inset-0 z-50 flex items-end justify-center bg-navy/50 p-0 backdrop-blur-[2px] sm:items-center sm:p-6"
      @click.self="close"
    >
      <div
        class="flex max-h-[92vh] w-full flex-col overflow-hidden rounded-t-2xl bg-white shadow-2xl sm:max-w-lg sm:rounded-2xl"
        role="dialog"
        aria-modal="true"
      >
        <div class="flex items-start justify-between gap-4 border-b border-line px-5 py-4 sm:px-6">
          <h2 class="text-lg font-bold">
            <slot name="title"></slot>
          </h2>
          <button
            type="button"
            class="btn btn-link btn-sm -mr-2 text-muted"
            :aria-label="$t('common.close')"
            @click="close"
          >
            <PhX :size="20" />
          </button>
        </div>
        <div class="overflow-y-auto px-5 py-5 sm:px-6">
          <slot name="body"></slot>
        </div>
        <div
          class="flex flex-col-reverse gap-2 border-t border-line bg-surface px-5 py-4 sm:flex-row sm:justify-end sm:px-6"
        >
          <button type="button" class="btn btn-outline-secondary" @click="close">
            {{ $t('common.close') }}
          </button>
          <slot name="buttons"></slot>
        </div>
      </div>
    </div>
  </Teleport>
</template>
