<script>
// Päringu staatuse märgis: U = uus, H = käsitletud
const STATUS_BADGE_CLASSES = {
  U: 'text-bg-primary',
  H: 'text-bg-secondary',
}

export default {
  name: 'EnquiryStatusBadge',
  props: {
    status: String,
  },
  computed: {
    statusBadgeClass() {
      return STATUS_BADGE_CLASSES[this.status]
    },
  },
}
</script>

<template>
  <span class="badge" :class="statusBadgeClass">{{ $t(`enquiryStatus.${status}`) }}</span>
</template>
