<script>
export default {
  name: 'TranslationFlags',
  props: {
    supportedLanguages: Array,
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
        ? this.$t('trainingForm.flags.open', { language: languageCode })
        : this.$t('trainingForm.flags.add', { language: languageCode })
    },
  },
}
</script>

<template>
  <div class="d-flex gap-2 align-items-center">
    <button
      v-for="supportedLanguage in supportedLanguages"
      :key="supportedLanguage.languageCode"
      @click="$emit('event-translation-flag-clicked', supportedLanguage.languageCode)"
      :title="flagTitle(supportedLanguage.languageCode)"
      :class="{
        'translation-missing': !translationExists(supportedLanguage.languageCode),
        'border-primary': supportedLanguage.languageCode === currentLanguageCode,
      }"
      class="btn btn-sm btn-light border"
      type="button"
    >
      <span class="fi" :class="supportedLanguage.flagClass"></span>
      {{ supportedLanguage.languageCode }}
    </button>
  </div>
</template>

<style scoped>
.translation-missing .fi {
  filter: grayscale(1);
  opacity: 0.4;
}
</style>
