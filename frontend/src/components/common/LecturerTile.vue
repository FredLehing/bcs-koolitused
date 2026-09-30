<script>
import LecturerAvatar from '@/components/common/LecturerAvatar.vue'

// "Meie koolitajad" kaart: kogu kaart on link koolitaja detailvaatesse
export default {
  name: 'LecturerTile',
  components: { LecturerAvatar },
  props: {
    lecturerSummary: Object,
  },
}
</script>

<template>
  <RouterLink
    :to="{ name: 'lecturerRoute', query: { lecturerId: lecturerSummary.lecturerId } }"
    class="card h-100 text-decoration-none text-body lecturer-tile"
  >
    <div class="card-body text-center">
      <LecturerAvatar
        :lecturer-id="lecturerSummary.lecturerId"
        :photo-version="lecturerSummary.photoVersion"
        :alt="lecturerSummary.fullName"
        :size="160"
        shape="rounded"
        class="mx-auto mb-3"
      />
      <div class="fw-bold">{{ lecturerSummary.fullName }}</div>
      <div class="small text-secondary mb-2">{{ lecturerSummary.title }}</div>
      <p class="small mb-0 short-description">{{ lecturerSummary.shortDescription }}</p>
    </div>
  </RouterLink>
</template>

<style scoped>
.lecturer-tile {
  transition: box-shadow 0.15s ease;
}

.lecturer-tile:hover,
.lecturer-tile:focus-visible {
  box-shadow: var(--bs-box-shadow);
}

/* Lühikirjeldus kuni 3 rida */
.short-description {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
