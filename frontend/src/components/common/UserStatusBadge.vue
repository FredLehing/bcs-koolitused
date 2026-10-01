<script>
// Konto staatuse märgis: A = aktiivne, D = deaktiveeritud
const STATUS_BADGE_CLASSES = {
  A: 'text-bg-success',
  D: 'text-bg-danger',
}

export default {
  name: 'UserStatusBadge',
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
  <span class="badge" :class="statusBadgeClass">{{ $t(`userStatus.${status}`) }}</span>
</template>
