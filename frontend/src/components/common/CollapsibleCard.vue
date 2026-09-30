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
      class="btn btn-link p-0 mb-2"
      type="button"
    >
      {{ isOpen ? hideLabel : showLabel }}
    </button>
    <div v-if="isOpen" :id="contentId" class="card mb-3">
      <div class="card-body">
        <slot></slot>
      </div>
    </div>
  </div>
</template>
