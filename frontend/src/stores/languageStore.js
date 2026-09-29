import { defineStore } from 'pinia'
import i18n, { getSavedLocale, LOCALE_STORAGE_KEY, UI_LANGUAGES } from '@/i18n.js'

// uiLanguages on kasutajaliidese keeled navbari keelevaliku jaoks (vt UI_LANGUAGES failis i18n.js).
// Andmebaasi keeled (tõlkekeeled, põhikeel, õppekeeled) siin ei ole — need tulevad backendist
// (GET /api/languages) ja vaade laadib need ise, kui vajab.
//
// contentLang on kasutajaliidese keel ja ühtlasi keel, milles backendist küsitakse tõlgitud andmeid
// (API parameeter contentLang). Keele vahetamine käib AINULT setContentLang() kaudu — see hoiab
// store'i, vue-i18n ja localStorage'i sünkis. Vaated jälgivad contentLang-i (watch) ja laadivad
// vajalikud andmed uuesti.
export const useLanguageStore = defineStore('language', {
  state: () => ({
    contentLang: getSavedLocale(),
    uiLanguages: UI_LANGUAGES,
  }),
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
