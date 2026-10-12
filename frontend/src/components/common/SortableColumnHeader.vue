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
  <th :aria-sort="ariaSort">
    <button
      @click="$emit('event-sort-clicked', sortKey)"
      class="inline-flex cursor-pointer items-center gap-1 font-bold tracking-wide uppercase hover:text-brand-700"
      :class="isActive ? 'text-brand-700' : 'text-muted'"
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
