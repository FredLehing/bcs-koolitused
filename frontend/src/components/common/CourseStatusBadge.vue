<script>
// Toimumiskorra staatuse märgis (U/O/F/X); möödunud toimumiskorral lisaks "Toimunud"
const STATUS_BADGE_CLASSES = {
  U: 'text-bg-secondary',
  O: 'text-bg-success',
  F: 'text-bg-warning',
  X: 'text-bg-danger',
}

export default {
  name: 'CourseStatusBadge',
  props: {
    status: String,
    isPast: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    statusBadgeClass() {
      return STATUS_BADGE_CLASSES[this.status]
    },
  },
}
</script>

<template>
  <span class="inline-flex flex-wrap gap-1">
    <span class="badge" :class="statusBadgeClass">{{ $t(`courseStatus.${status}`) }}</span>
    <span v-if="isPast" class="badge text-bg-light">{{ $t('courseStatus.past') }}</span>
  </span>
</template>
