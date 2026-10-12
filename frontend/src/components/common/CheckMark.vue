<script>
import { PhCheck, PhX } from '@phosphor-icons/vue'

// ✓ (roheline) või ✗ (punane) tabeli lahtris
export default {
  name: 'CheckMark',
  components: { PhCheck, PhX },
  props: {
    value: Boolean,
    title: {
      type: String,
      default: '',
    },
  },
}
</script>

<template>
  <span :title="title" :aria-label="title" role="img" class="inline-flex">
    <PhCheck v-if="value" :size="20" weight="bold" class="text-emerald-700" />
    <PhX v-else :size="20" weight="bold" class="text-red-600" />
  </span>
</template>
