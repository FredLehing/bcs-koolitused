<script>
import FlagIcon from '@/components/common/FlagIcon.vue'

export default {
  name: 'TranslationFlags',
  components: { FlagIcon },
  props: {
    translationLanguages: Array,
    // Olemasolevad tõlked (vähemalt languageCode väli) — nt koolituse või koolitaja tõlked
    existingTranslations: Array,
    currentLanguageCode: String,
  },
  emits: ['event-translation-flag-clicked'],
  methods: {
    translationExists(languageCode) {
      return this.existingTranslations.some(
        (existingTranslation) => existingTranslation.languageCode === languageCode,
      )
    },

    flagTitle(languageCode) {
      return this.translationExists(languageCode)
        ? this.$t('translationFlags.open', { language: languageCode })
        : this.$t('translationFlags.add', { language: languageCode })
    },
  },
}
</script>

<template>
  <div class="d-flex gap-2 align-items-center">
    <button
      v-for="translationLanguage in translationLanguages"
      :key="translationLanguage.languageCode"
      @click="$emit('event-translation-flag-clicked', translationLanguage)"
      :title="flagTitle(translationLanguage.languageCode)"
      :class="{
        'translation-missing': !translationExists(translationLanguage.languageCode),
        'border-primary': translationLanguage.languageCode === currentLanguageCode,
      }"
      class="btn btn-sm btn-light border"
      type="button"
    >
      <FlagIcon :flag-icon-code="translationLanguage.flagIconCode" />
      {{ translationLanguage.languageCode }}
    </button>
  </div>
</template>

<style scoped>
.translation-missing .fi {
  filter: grayscale(1);
  opacity: 0.4;
}
</style>
