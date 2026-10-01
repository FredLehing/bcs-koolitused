<script>
import CurriculumUpload from '@/components/forms/CurriculumUpload.vue'
import RichTextEditor from '@/components/forms/RichTextEditor.vue'

export default {
  name: 'TrainingTranslationForm',
  components: { RichTextEditor, CurriculumUpload },
  props: {
    translation: Object,
    newCurriculum: Object,
    isCurriculumRemoved: Boolean,
    isSaving: Boolean,
    languageName: String,
    showAiButton: {
      type: Boolean,
      default: false,
    },
    isAiLoading: {
      type: Boolean,
      default: false,
    },
    aiTooltip: String,
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
    'event-new-title-input',
    'event-new-short-description-input',
    'event-new-description-input',
    'event-ai-translation-clicked',
    'event-ai-pdf-clicked',
  ],
}
</script>

<template>
  <fieldset class="border rounded bg-body p-3 mb-4" :disabled="isDisabled">
    <legend class="float-none w-auto px-2 fs-5">
      {{ $t('trainingForm.translation.legend', { language: languageName }) }}
    </legend>
    <div class="text-start">
      <div v-if="showAiButton" class="mb-3">
        <button
          @click="$emit('event-ai-translation-clicked')"
          :disabled="isDisabled"
          :title="aiTooltip"
          class="btn btn-outline-primary btn-sm"
          type="button"
        >
          <span v-if="isAiLoading" class="spinner-border spinner-border-sm me-1"></span>
          {{
            isAiLoading
              ? $t('trainingForm.translation.aiLoading')
              : $t('trainingForm.translation.aiButton')
          }}
        </button>
      </div>
      <div class="mb-3">
        <label class="form-label" for="title">{{ $t('trainingForm.translation.title') }}</label>
        <input
          :value="translation.title"
          @input="$emit('event-new-title-input', $event.target.value)"
          id="title"
          class="form-control"
          type="text"
          maxlength="255"
        />
      </div>
      <div class="mb-3">
        <label class="form-label" for="shortDescription">{{
          $t('trainingForm.translation.shortDescription')
        }}</label>
        <input
          :value="translation.shortDescription"
          @input="$emit('event-new-short-description-input', $event.target.value)"
          id="shortDescription"
          class="form-control"
          type="text"
          maxlength="255"
        />
      </div>
      <div>
        <label class="form-label" id="descriptionLabel">{{
          $t('trainingForm.translation.description')
        }}</label>
        <RichTextEditor
          :html="translation.description"
          :is-disabled="isDisabled"
          @event-new-html-input="$emit('event-new-description-input', $event)"
          label-id="descriptionLabel"
        />
      </div>
      <CurriculumUpload
        :key="`${translation.trainingTranslationId}-${translation.languageCode}`"
        :training-translation-id="translation.trainingTranslationId"
        :curriculum-file-name="translation.curriculumFileName"
        :curriculum-file-size="translation.curriculumFileSize"
        :new-curriculum="newCurriculum"
        :is-curriculum-removed="isCurriculumRemoved"
        :is-disabled="isSaving || isDisabled"
        :show-ai-pdf-button="showAiPdfButton"
        :is-ai-pdf-loading="isAiPdfLoading"
        :ai-pdf-tooltip="aiPdfTooltip"
        @event-ai-pdf-clicked="$emit('event-ai-pdf-clicked')"
        @event-curriculum-selected="$emit('event-curriculum-selected', $event)"
        @event-curriculum-removed="$emit('event-curriculum-removed')"
        @event-curriculum-error="$emit('event-curriculum-error', $event)"
        @event-curriculum-loading="$emit('event-curriculum-loading', $event)"
      />
    </div>
  </fieldset>
</template>
