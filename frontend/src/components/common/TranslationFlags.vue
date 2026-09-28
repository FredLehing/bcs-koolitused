<script>
export default {
  name: 'TranslationFlags',
  props: {
    contentLanguages: Array,
    trainingTranslations: Array,
    currentLanguageCode: String,
  },
  emits: ['event-translation-flag-clicked'],
  methods: {
    translationExists(languageCode) {
      return this.trainingTranslations.some(
        (trainingTranslation) => trainingTranslation.languageCode === languageCode,
      )
    },

    flagTitle(languageCode) {
      return this.translationExists(languageCode)
        ? 'Ava ' + languageCode + ' tõlge'
        : 'Lisa ' + languageCode + ' tõlge'
    },
  },
}
</script>

<template>
  <div class="d-flex gap-2 align-items-center">
    <button
      v-for="contentLanguage in contentLanguages"
      :key="contentLanguage.languageCode"
      @click="$emit('event-translation-flag-clicked', contentLanguage.languageCode)"
      :title="flagTitle(contentLanguage.languageCode)"
      :class="{
        'translation-missing': !translationExists(contentLanguage.languageCode),
        'border-primary': contentLanguage.languageCode === currentLanguageCode,
      }"
      class="btn btn-sm btn-light border"
      type="button"
    >
      <span class="fi" :class="contentLanguage.languageFlag"></span>
      {{ contentLanguage.languageCode }}
    </button>
  </div>
</template>

<style scoped>
.translation-missing .fi {
  filter: grayscale(1);
  opacity: 0.4;
}
</style>
