import { defineStore } from 'pinia'
import i18n, { getSavedLocale, LOCALE_STORAGE_KEY } from '@/i18n.js'

// Süsteemi sisukeeled (tõlgete keeled), frontendis hard coded.
// Peab ühtima andmebaasi language tabeliga (languageCode, is_main_language).
// flagClass on flag-icons klass (https://flagicons.lipis.dev/).
//
// contentLang on kasutajaliidese keel ja ühtlasi keel, milles backendist küsitakse tõlgitud andmeid
// (API parameeter contentLang). Keele vahetamine käib AINULT setContentLang() kaudu — see hoiab
// store'i, vue-i18n ja localStorage'i sünkis. Vaated jälgivad contentLang-i (watch) ja laadivad
// vajalikud andmed uuesti.
export const useLanguageStore = defineStore('language', {
  state: () => ({
    contentLang: getSavedLocale(),
    supportedLanguages: [
      { languageCode: 'et', isMainLanguage: true, flagClass: 'fi-ee' },
      { languageCode: 'en', isMainLanguage: false, flagClass: 'fi-gb' },
    ],
  }),
  getters: {
    mainLanguageCode: (state) =>
      state.supportedLanguages.find((supportedLanguage) => supportedLanguage.isMainLanguage)
        .languageCode,

    // Getter, mis tagastab funktsiooni — nii saab getterile argumendi anda: getFlagClass('et')
    getFlagClass: (state) => (languageCode) =>
      state.supportedLanguages.find(
        (supportedLanguage) => supportedLanguage.languageCode === languageCode,
      )?.flagClass ?? '',
  },
  actions: {
    setContentLang(languageCode) {
      this.contentLang = languageCode
      i18n.global.locale.value = languageCode
      try {
        localStorage.setItem(LOCALE_STORAGE_KEY, languageCode)
      } catch {
        // localStorage võib olla keelatud — keel kehtib siis ainult selle seansi ajal
      }
    },
  },
})
