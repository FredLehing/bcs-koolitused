<script>
import { PhCaretDown, PhCaretUp } from '@phosphor-icons/vue'

// Sorteeritava tabeli veeru pealkiri. Nool on nähtav ainult aktiivsel veerul (sortBy === sortKey).
export default {
  name: 'SortableColumnHeader',
  components: { PhCaretDown, PhCaretUp },
  props: {
    label: String,
    sortKey: String,
    sortBy: String,
    sortDirection: String,
  },
  emits: ['event-sort-clicked'],
  computed: {
    isActive() {
      return this.sortBy === this.sortKey
    },

    ariaSort() {
      if (!this.isActive) {
        return 'none'
      }
      return this.sortDirection === 'asc' ? 'ascending' : 'descending'
    },
  },
}
</script>

<template>
  <th :aria-sort="ariaSort" class="text-nowrap">
    <button
      @click="$emit('event-sort-clicked', sortKey)"
      class="btn btn-link p-0 fw-semibold text-body text-decoration-none d-inline-flex align-items-center gap-1"
      type="button"
    >
      {{ label }}
      <template v-if="isActive">
        <PhCaretUp v-if="sortDirection === 'asc'" :size="14" weight="bold" />
        <PhCaretDown v-else :size="14" weight="bold" />
      </template>
    </button>
  </th>
</template>
