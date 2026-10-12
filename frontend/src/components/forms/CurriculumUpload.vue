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
    showAiPdfButton: Boolean,
    isAiPdfLoading: Boolean,
    aiPdfTooltip: String,
  },
  emits: [
    'event-curriculum-selected',
    'event-curriculum-removed',
    'event-curriculum-error',
    'event-curriculum-loading',
    'event-ai-pdf-clicked',
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
          file: file,
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
  <fieldset class="group mt-4" :disabled="isDisabled || fileReader !== null">
    <legend class="form-label">{{ $t('trainingForm.translation.curriculumField') }}</legend>
    <div class="flex flex-wrap items-start gap-3">
      <div v-if="newCurriculum" class="break-words">
        {{ newCurriculum.fileName }} ({{ formatSize(newCurriculum.fileSize) }})
        <div class="form-text">{{ $t('trainingForm.translation.curriculumPending') }}</div>
        <button type="button" class="btn btn-outline-secondary btn-sm mt-2" @click="undo">
          {{ $t('trainingForm.translation.curriculumUndo') }}
        </button>
      </div>
      <div v-else-if="isCurriculumRemoved" class="break-words">
        <s>{{ curriculumFileName }}</s>
        <div class="form-text">{{ $t('trainingForm.translation.curriculumRemoving') }}</div>
        <button type="button" class="btn btn-outline-secondary btn-sm mt-2" @click="undo">
          {{ $t('trainingForm.translation.curriculumUndo') }}
        </button>
      </div>
      <div v-else>
        <div v-if="curriculumFileName" class="mb-2 break-words">
          <a :href="curriculumUrl">{{ curriculumFileName }}</a>
          ({{ formatSize(curriculumFileSize) }})
        </div>
        <div class="flex flex-wrap gap-2">
          <label
            class="btn btn-outline-primary btn-sm focus-within:outline-2 focus-within:outline-offset-2 focus-within:outline-brand-600 group-disabled:pointer-events-none group-disabled:opacity-50"
          >
            {{
              $t(
                `trainingForm.translation.${curriculumFileName ? 'curriculumChange' : 'curriculumSelect'}`,
              )
            }}
            <input
              type="file"
              accept="application/pdf"
              class="sr-only"
              :aria-label="$t('trainingForm.translation.curriculumSelect')"
              @change="handleFileSelected"
            />
          </label>
          <button
            v-if="curriculumFileName"
            type="button"
            class="btn btn-outline-danger btn-sm"
            @click="remove"
          >
            {{ $t('trainingForm.translation.curriculumRemove') }}
          </button>
        </div>
        <div class="form-text">{{ $t('trainingForm.translation.curriculumHint') }}</div>
      </div>
      <button
        v-if="showAiPdfButton"
        type="button"
        class="btn btn-outline-primary btn-sm"
        :title="aiPdfTooltip"
        @click="$emit('event-ai-pdf-clicked')"
      >
        <span
          v-if="isAiPdfLoading"
          class="inline-block size-4 animate-spin rounded-full border-2 border-current border-r-transparent"
          aria-hidden="true"
        ></span>
        {{ $t(`trainingForm.translation.${isAiPdfLoading ? 'aiPdfLoading' : 'aiPdfButton'}`) }}
      </button>
    </div>
    <div v-if="errorKey" class="mt-1 text-sm text-red-700" role="alert">
      {{ $t(`trainingForm.translation.${errorKey}`) }}
    </div>
  </fieldset>
</template>
