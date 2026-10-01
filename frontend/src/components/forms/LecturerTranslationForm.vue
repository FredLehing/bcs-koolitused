<script>
import RichTextEditor from '@/components/forms/RichTextEditor.vue'
import FlagIcon from '@/components/common/FlagIcon.vue'
import HelpTip from '@/components/common/HelpTip.vue'

export default {
  name: 'LecturerTranslationForm',
  components: { RichTextEditor, FlagIcon, HelpTip },
  props: {
    translation: Object,
    languageName: String,
    flagIconCode: String,
    showAiButton: {
      type: Boolean,
      default: false,
    },
    isAiLoading: {
      type: Boolean,
      default: false,
    },
    showPrefilledHint: {
      type: Boolean,
      default: false,
    },
    mainLanguageCode: String,
  },
  emits: [
    'event-new-title-input',
    'event-new-short-description-input',
    'event-new-description-input',
    'event-ai-translation-clicked',
  ],
}
</script>

<template>
  <section
    class="rounded-2xl border border-line bg-white p-6"
    aria-labelledby="lecturer-translation-heading"
  >
    <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
      <h2 id="lecturer-translation-heading" class="flex items-center gap-2 text-lg font-bold">
        <FlagIcon v-if="flagIconCode" :flag-icon-code="flagIconCode" />
        {{ $t('lecturerForm.translation.legend', { language: languageName }) }}
      </h2>
      <button
        v-if="showAiButton"
        @click="$emit('event-ai-translation-clicked')"
        :disabled="isAiLoading"
        :title="$t('lecturerForm.translation.aiTooltip')"
        class="btn btn-outline-primary btn-sm"
        type="button"
      >
        <span
          v-if="isAiLoading"
          class="inline-block size-4 animate-spin rounded-full border-2 border-current border-r-transparent"
          aria-hidden="true"
        ></span>
        {{
          isAiLoading
            ? $t('trainingForm.translation.aiLoading')
            : $t('trainingForm.translation.aiButton')
        }}
      </button>
    </div>
    <p v-if="showPrefilledHint" class="mb-4 text-sm text-muted">
      {{ $t('lecturerForm.translation.prefilledHint', { mainLanguage: mainLanguageCode }) }}
    </p>
    <div class="grid gap-4 md:grid-cols-2">
      <div>
        <div class="mb-1.5 flex items-center gap-1">
          <label class="form-label mb-0" for="lecturerTitle">
            {{ $t('lecturerForm.translation.title') }} *
          </label>
          <HelpTip :text="$t('lecturerForm.translation.titleHelp')" align="left" class="-my-3" />
        </div>
        <input
          :value="translation.title"
          @input="$emit('event-new-title-input', $event.target.value)"
          :placeholder="$t('lecturerForm.translation.titlePlaceholder')"
          id="lecturerTitle"
          class="form-control"
          type="text"
          maxlength="255"
        />
      </div>
      <div>
        <label class="form-label" for="lecturerShortDescription"
          >{{ $t('lecturerForm.translation.shortDescription') }} *</label
        >
        <input
          :value="translation.shortDescription"
          @input="$emit('event-new-short-description-input', $event.target.value)"
          id="lecturerShortDescription"
          class="form-control"
          type="text"
          maxlength="255"
        />
        <div class="form-text">{{ $t('lecturerForm.translation.shortDescriptionHint') }}</div>
      </div>
      <div class="md:col-span-2">
        <label class="form-label" id="lecturerDescriptionLabel"
          >{{ $t('lecturerForm.translation.description') }} *</label
        >
        <RichTextEditor
          :html="translation.description"
          @event-new-html-input="$emit('event-new-description-input', $event)"
          label-id="lecturerDescriptionLabel"
        />
      </div>
    </div>
  </section>
</template>
