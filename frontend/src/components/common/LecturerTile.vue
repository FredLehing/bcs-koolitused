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
    :to="{
      name: 'lecturerRoute',
      query: { returnTo: $route.fullPath, lecturerId: lecturerSummary.lecturerId },
    }"
    class="flex h-full flex-col items-center rounded-2xl border border-line bg-white p-5 text-center text-ink transition-shadow hover:text-ink hover:shadow-lg hover:shadow-brand-600/10 focus-visible:shadow-lg focus-visible:shadow-brand-600/10"
  >
    <LecturerAvatar
      :lecturer-id="lecturerSummary.lecturerId"
      :photo-version="lecturerSummary.photoVersion"
      :alt="lecturerSummary.fullName"
      :size="160"
      shape="rounded"
      class="mb-4"
    />
    <div class="font-display text-lg font-bold text-navy">{{ lecturerSummary.fullName }}</div>
    <div class="mb-2 text-sm text-muted">{{ lecturerSummary.title }}</div>
    <p class="line-clamp-3 text-sm">{{ lecturerSummary.shortDescription }}</p>
  </RouterLink>
</template>
