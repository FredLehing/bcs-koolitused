<script>
export default {
  name: 'TrainingTranslationForm',
  props: {
    translation: Object,
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
  <fieldset class="border rounded p-3 mb-4">
    <legend class="float-none w-auto px-2 fs-5">Tõlge: {{ languageName }}</legend>
    <div class="text-start">
      <div v-if="showAiButton" class="mb-3">
        <button
          @click="$emit('event-ai-translation-clicked')"
          :disabled="isAiLoading"
          :title="aiTooltip"
          class="btn btn-outline-primary btn-sm"
          type="button"
        >
          <span v-if="isAiLoading" class="spinner-border spinner-border-sm me-1"></span>
          {{ isAiLoading ? 'Tõlgin…' : 'Tee AI tõlge' }}
        </button>
      </div>
      <div class="mb-3">
        <label class="form-label" for="title">Pealkiri</label>
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
        <label class="form-label" for="shortDescription">Lühikirjeldus</label>
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
        <!-- TODO: richtext editor (praegu tavaline textarea) -->
        <label class="form-label" for="description">Kirjeldus</label>
        <textarea
          :value="translation.description"
          @input="$emit('event-new-description-input', $event.target.value)"
          id="description"
          class="form-control"
          rows="6"
        ></textarea>
      </div>
    </div>
  </fieldset>
</template>
