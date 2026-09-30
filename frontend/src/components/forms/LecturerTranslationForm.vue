<script>
import { mapState } from 'pinia'
import { Tooltip } from 'bootstrap'
import { useLanguageStore } from '@/stores/languageStore.js'
import { PhQuestion } from '@phosphor-icons/vue'
import RichTextEditor from '@/components/forms/RichTextEditor.vue'
import FlagIcon from '@/components/common/FlagIcon.vue'

export default {
  name: 'LecturerTranslationForm',
  components: { PhQuestion, RichTextEditor, FlagIcon },
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
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),
  },
  watch: {
    contentLang() {
      this.$nextTick(() => this.updateTitleHelpTooltip())
    },
  },
  methods: {
    // Bootstrap tooltip loeb teksti ainult loomisel, keele vahetusel tuleb see uuendada
    updateTitleHelpTooltip() {
      this.titleHelpTooltip.setContent({
        '.tooltip-inner': this.$t('lecturerForm.translation.titleHelp'),
      })
    },
  },
  mounted() {
    this.titleHelpTooltip = new Tooltip(this.$refs.titleHelp)
  },
  beforeUnmount() {
    this.titleHelpTooltip.dispose()
  },
}
</script>

<template>
  <fieldset class="border rounded p-3 mb-4">
    <legend class="float-none w-auto px-2 fs-5">
      <FlagIcon v-if="flagIconCode" :flag-icon-code="flagIconCode" />
      {{ $t('lecturerForm.translation.legend', { language: languageName }) }}
    </legend>
    <div class="text-start">
      <p v-if="showPrefilledHint" class="small text-secondary">
        {{ $t('lecturerForm.translation.prefilledHint', { mainLanguage: mainLanguageCode }) }}
      </p>
      <div v-if="showAiButton" class="mb-3">
        <button
          @click="$emit('event-ai-translation-clicked')"
          :disabled="isAiLoading"
          :title="$t('lecturerForm.translation.aiTooltip')"
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
        <label class="form-label d-inline-flex align-items-center gap-1" for="lecturerTitle">
          {{ $t('lecturerForm.translation.title') }} *
          <span
            ref="titleHelp"
            class="text-secondary d-inline-flex"
            role="img"
            tabindex="0"
            data-bs-toggle="tooltip"
            :data-bs-title="$t('lecturerForm.translation.titleHelp')"
            :aria-label="$t('lecturerForm.translation.titleHelp')"
          >
            <PhQuestion :size="18" />
          </span>
        </label>
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
      <div class="mb-3">
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
      <div>
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
  </fieldset>
</template>
