import { defineStore } from 'pinia'

// Süsteemi sisukeeled (tõlgete keeled), frontendis hard coded.
// Peab ühtima andmebaasi language tabeliga (languageCode, is_main_language).
// languageFlag on flag-icons klass (https://flagicons.lipis.dev/).
export const useLanguageStore = defineStore('language', {
  state: () => ({
    contentLanguages: [
      { languageCode: 'et', isMainLanguage: true, languageFlag: 'fi-ee' },
      { languageCode: 'en', isMainLanguage: false, languageFlag: 'fi-gb' },
    ],
  }),
  getters: {
    mainLanguageCode: (state) =>
      state.contentLanguages.find((contentLanguage) => contentLanguage.isMainLanguage).languageCode,
  },
})
