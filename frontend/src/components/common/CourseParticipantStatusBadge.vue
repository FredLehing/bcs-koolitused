<script>
// Registreerumise staatuse märgis: R = registreerunud, C = loobunud
const STATUS_BADGE_CLASSES = {
  R: 'text-bg-primary',
  C: 'text-bg-secondary',
}

export default {
  name: 'CourseParticipantStatusBadge',
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
  <span class="badge" :class="statusBadgeClass">
    {{ $t(`courseParticipantStatus.${status}`) }}
  </span>
</template>
