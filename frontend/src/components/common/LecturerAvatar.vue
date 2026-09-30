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
      return this.shape === 'rounded' ? 'rounded-3' : 'rounded-circle'
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
    class="lecturer-avatar flex-shrink-0"
    loading="lazy"
  />
  <div
    v-else
    :style="sizeStyle"
    :class="shapeClass"
    class="lecturer-avatar-placeholder flex-shrink-0 d-flex align-items-center justify-content-center bg-body-secondary text-secondary"
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
