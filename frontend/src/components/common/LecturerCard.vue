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
    class="-mx-2 flex items-start gap-3 rounded-xl p-2 text-ink transition-colors hover:bg-brand-50 hover:text-ink"
  >
    <LecturerAvatar
      :lecturer-id="lecturerSummary.lecturerId"
      :photo-version="lecturerSummary.photoVersion"
      :alt="lecturerSummary.fullName"
      :size="56"
    />
    <div class="min-w-0">
      <div class="font-semibold text-navy">{{ lecturerSummary.fullName }}</div>
      <div class="mb-1 text-sm text-muted">{{ lecturerSummary.title }}</div>
      <div class="line-clamp-3 text-sm">{{ lecturerSummary.shortDescription }}</div>
    </div>
  </RouterLink>
</template>
