<script>
import LecturerAvatar from '@/components/common/LecturerAvatar.vue'

const ALLOWED_CONTENT_TYPES = ['image/png', 'image/jpeg', 'image/webp']
const MAX_PHOTO_BYTES = 2 * 1024 * 1024

// Koolitaja pildi valik: eelvaade (salvestatud pilt pilditeenusest või uue pildi data URL),
// "Vali pilt" ja "Eemalda". Tüüp ja suurus kontrollitakse enne; kärpimist ega vähendamist ei tehta
// (backend normaliseerib pildi).
export default {
  name: 'PhotoUpload',
  components: { LecturerAvatar },
  props: {
    lecturerId: Number,
    // Salvestatud pildi versioon (null = salvestatud pilti pole)
    photoVersion: {
      type: Number,
      default: null,
    },
    // Uus valitud pilt: { photo: Base64, photoContentType } või null
    newPhoto: {
      type: Object,
      default: null,
    },
    isPhotoRemoved: {
      type: Boolean,
      default: false,
    },
    isReadonly: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-photo-selected', 'event-photo-removed', 'event-photo-error'],
  computed: {
    previewSrc() {
      return this.newPhoto
        ? `data:${this.newPhoto.photoContentType};base64,${this.newPhoto.photo}`
        : null
    },

    shownPhotoVersion() {
      return this.isPhotoRemoved ? null : this.photoVersion
    },

    hasPhoto() {
      return this.newPhoto !== null || this.shownPhotoVersion !== null
    },
  },
  methods: {
    handleFileSelected(event) {
      const file = event.target.files[0]
      event.target.value = ''
      if (!file) {
        return
      }
      if (!ALLOWED_CONTENT_TYPES.includes(file.type) || file.size > MAX_PHOTO_BYTES) {
        this.$emit('event-photo-error', this.$t('photoUpload.invalidFile'))
        return
      }
      const fileReader = new FileReader()
      fileReader.onload = () => this.handleFileRead(fileReader.result, file.type)
      fileReader.readAsDataURL(file)
    },

    // data URL "data:image/png;base64,iVBOR..." → Base64 osa pärast koma
    handleFileRead(dataUrl, contentType) {
      this.$emit('event-photo-selected', {
        photo: dataUrl.substring(dataUrl.indexOf(',') + 1),
        photoContentType: contentType,
      })
    },
  },
}
</script>

<template>
  <div class="d-flex align-items-center gap-3">
    <LecturerAvatar
      :lecturer-id="lecturerId"
      :photo-version="shownPhotoVersion"
      :preview-src="previewSrc"
      :size="80"
    />
    <div v-if="!isReadonly">
      <div class="d-flex flex-wrap gap-2 mb-1">
        <label class="btn btn-sm btn-outline-primary mb-0">
          {{ $t('photoUpload.select') }}
          <input
            @change="handleFileSelected"
            accept="image/png,image/jpeg,image/webp"
            class="d-none"
            type="file"
          />
        </label>
        <button
          v-if="hasPhoto"
          @click="$emit('event-photo-removed')"
          class="btn btn-sm btn-outline-danger"
          type="button"
        >
          {{ $t('photoUpload.remove') }}
        </button>
      </div>
      <div class="form-text">{{ $t('photoUpload.hint') }}</div>
    </div>
  </div>
</template>
