<script>
import LecturerAvatar from '@/components/common/LecturerAvatar.vue'

// Kuvab koondpäringuga saadud kaardi andmed. JSON-päringuid komponent ei tee.
export default {
  name: 'LecturerCard',
  components: { LecturerAvatar },
  props: {
    lecturerSummary: { type: Object, required: true },
  },
}
</script>

<template>
  <RouterLink
    :to="{
      name: 'lecturerRoute',
      query: { lecturerId: lecturerSummary.lecturerId, returnTo: $route.fullPath },
    }"
    :aria-label="$t('lecturerCard.viewProfile', { name: lecturerSummary.fullName })"
    class="d-flex gap-3 align-items-start rounded p-2 text-decoration-none text-body lecturer-card-link"
  >
    <LecturerAvatar
      :lecturer-id="lecturerSummary.lecturerId"
      :photo-version="lecturerSummary.photoVersion"
      :alt="lecturerSummary.fullName"
      :size="56"
    />
    <div>
      <div class="fw-bold">{{ lecturerSummary.fullName }}</div>
      <div class="small text-secondary mb-1">{{ lecturerSummary.title }}</div>
      <div class="small">{{ lecturerSummary.shortDescription }}</div>
    </div>
  </RouterLink>
</template>

<style scoped>
/* Kogu kaart on link koolitaja detailvaatesse */
.lecturer-card-link {
  transition: box-shadow 0.15s ease;
}

.lecturer-card-link:hover,
.lecturer-card-link:focus-visible {
  box-shadow: var(--bs-box-shadow);
}
</style>
