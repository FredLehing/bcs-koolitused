<script>
import { PhUser } from '@phosphor-icons/vue'
import LecturerService from '@/api-services/LecturerService.js'

// Koolitaja pilt pilditeenusest (photoVersion = null → kohatäide). previewSrc (nt vormis valitud uue
// pildi data URL) on eelistatud salvestatud pildile.
export default {
  name: 'LecturerAvatar',
  components: { PhUser },
  props: {
    lecturerId: Number,
    photoVersion: {
      type: Number,
      default: null,
    },
    previewSrc: {
      type: String,
      default: null,
    },
    size: {
      type: Number,
      default: 56,
    },
    // circle = ümar (kaart), rounded = ümarate nurkadega ruut (nimekiri, detailvaade)
    shape: {
      type: String,
      default: 'circle',
    },
    alt: {
      type: String,
      default: '',
    },
  },
  computed: {
    photoSrc() {
      if (this.previewSrc) {
        return this.previewSrc
      }
      if (this.photoVersion === null || this.photoVersion === undefined) {
        return null
      }
      return LecturerService.getLecturerPhotoUrl(this.lecturerId, this.photoVersion)
    },

    sizeStyle() {
      return { width: `${this.size}px`, height: `${this.size}px` }
    },

    shapeClass() {
      return this.shape === 'rounded' ? 'rounded-xl' : 'rounded-full'
    },
  },
}
</script>

<template>
  <img
    v-if="photoSrc"
    :src="photoSrc"
    :alt="alt"
    :style="sizeStyle"
    :class="shapeClass"
    class="lecturer-avatar shrink-0"
    loading="lazy"
  />
  <div
    v-else
    :style="sizeStyle"
    :class="shapeClass"
    class="flex shrink-0 items-center justify-center bg-brand-100 text-brand-600"
    role="img"
    :aria-label="alt"
  >
    <PhUser :size="Math.round(size * 0.55)" />
  </div>
</template>

<style scoped>
.lecturer-avatar {
  object-fit: cover;
}
</style>
