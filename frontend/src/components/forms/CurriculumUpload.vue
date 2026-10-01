<script>
import TrainingService from '@/api-services/TrainingService.js'

const MAX_CURRICULUM_BYTES = 10 * 1024 * 1024

export default {
  name: 'CurriculumUpload',
  props: {
    trainingTranslationId: Number,
    curriculumFileName: String,
    curriculumFileSize: Number,
    newCurriculum: Object,
    isCurriculumRemoved: Boolean,
    isDisabled: Boolean,
  },
  emits: [
    'event-curriculum-selected',
    'event-curriculum-removed',
    'event-curriculum-error',
    'event-curriculum-loading',
  ],
  data() {
    return { errorKey: '', fileReader: null }
  },
  computed: {
    curriculumUrl() {
      return TrainingService.getCurriculumUrl(this.trainingTranslationId)
    },
  },
  methods: {
    formatSize(size) {
      const useKilobytes = size < 100 * 1024
      const value = size / (useKilobytes ? 1024 : 1024 * 1024)
      return `${value.toLocaleString('et-EE', { minimumFractionDigits: 1, maximumFractionDigits: 1 })} ${useKilobytes ? 'KB' : 'MB'}`
    },

    handleFileSelected(event) {
      const file = event.target.files[0]
      event.target.value = ''
      if (!file) return
      this.setError('')
      if (file.type !== 'application/pdf' || file.size > MAX_CURRICULUM_BYTES) {
        this.setError('curriculumInvalid')
        return
      }
      this.fileReader = new FileReader()
      this.$emit('event-curriculum-loading', true)
      this.fileReader.onload = () => {
        this.$emit('event-curriculum-selected', {
          curriculum: this.fileReader.result.split(',')[1],
          fileName: file.name,
          fileSize: file.size,
        })
      }
      this.fileReader.onerror = () => this.setError('curriculumReadError')
      this.fileReader.onloadend = () => {
        this.fileReader = null
        this.$emit('event-curriculum-loading', false)
      }
      this.fileReader.readAsDataURL(file)
    },

    setError(key) {
      this.errorKey = key
      this.$emit('event-curriculum-error', key ? this.$t(`trainingForm.translation.${key}`) : '')
    },

    undo() {
      this.setError('')
      this.$emit('event-curriculum-selected', null)
    },

    remove() {
      this.setError('')
      this.$emit('event-curriculum-removed')
    },
  },
  beforeUnmount() {
    if (this.fileReader) {
      this.fileReader.onload = null
      this.fileReader.onerror = null
      this.fileReader.onloadend = null
      this.fileReader.abort()
    }
  },
}
</script>

<template>
  <fieldset class="mt-3" :disabled="isDisabled || fileReader !== null">
    <legend class="form-label fs-6">{{ $t('trainingForm.translation.curriculumField') }}</legend>
    <div v-if="newCurriculum" class="text-break">
      {{ newCurriculum.fileName }} ({{ formatSize(newCurriculum.fileSize) }})
      <div class="form-text">{{ $t('trainingForm.translation.curriculumPending') }}</div>
      <button type="button" class="btn btn-sm btn-outline-secondary mt-2" @click="undo">
        {{ $t('trainingForm.translation.curriculumUndo') }}
      </button>
    </div>
    <div v-else-if="isCurriculumRemoved" class="text-break">
      <s>{{ curriculumFileName }}</s>
      <div class="form-text">{{ $t('trainingForm.translation.curriculumRemoving') }}</div>
      <button type="button" class="btn btn-sm btn-outline-secondary mt-2" @click="undo">
        {{ $t('trainingForm.translation.curriculumUndo') }}
      </button>
    </div>
    <div v-else>
      <div v-if="curriculumFileName" class="mb-2 text-break">
        <a :href="curriculumUrl">{{ curriculumFileName }}</a>
        ({{ formatSize(curriculumFileSize) }})
      </div>
      <div class="d-flex flex-wrap gap-2">
        <label class="btn btn-sm btn-outline-primary mb-0">
          {{
            $t(
              `trainingForm.translation.${curriculumFileName ? 'curriculumChange' : 'curriculumSelect'}`,
            )
          }}
          <input
            type="file"
            accept="application/pdf"
            class="visually-hidden"
            :aria-label="$t('trainingForm.translation.curriculumSelect')"
            @change="handleFileSelected"
          />
        </label>
        <button
          v-if="curriculumFileName"
          type="button"
          class="btn btn-sm btn-outline-danger"
          @click="remove"
        >
          {{ $t('trainingForm.translation.curriculumRemove') }}
        </button>
      </div>
      <div class="form-text">{{ $t('trainingForm.translation.curriculumHint') }}</div>
    </div>
    <div v-if="errorKey" class="text-danger mt-1" role="alert">
      {{ $t(`trainingForm.translation.${errorKey}`) }}
    </div>
  </fieldset>
</template>
