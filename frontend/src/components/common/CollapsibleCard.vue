<script>
// Link "▾ Näita … / ▴ Peida …" ja selle all kaart (sisu slotis). Avatud olekut hoiab vaade.
export default {
  name: 'CollapsibleCard',
  props: {
    isOpen: Boolean,
    showLabel: String,
    hideLabel: String,
    contentId: String,
  },
  emits: ['event-toggle-clicked'],
}
</script>

<template>
  <div>
    <button
      @click="$emit('event-toggle-clicked')"
      :aria-expanded="isOpen"
      :aria-controls="contentId"
      class="btn btn-link mb-2 py-0"
      type="button"
    >
      {{ isOpen ? hideLabel : showLabel }}
    </button>
    <div
      v-if="isOpen"
      :id="contentId"
      class="mb-4 rounded-2xl border border-line bg-white p-5 sm:p-6"
    >
      <slot></slot>
    </div>
  </div>
</template>
