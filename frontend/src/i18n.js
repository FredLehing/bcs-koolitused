import { createI18n } from 'vue-i18n'
import et from '@/locales/et.json'
import en from '@/locales/en.json'

// Kasutajaliidese keel salvestatakse localStorage'isse (sama võti, mida kasutab sisukeel contentLang)
export const LOCALE_STORAGE_KEY = 'contentLang'

// Kasutajaliidese keeled (navbari keelevalik). Hard coded, sest iga keele jaoks peab olemas olema
// tõlkefail src/locales/<languageCode>.json.
// flagIconCode on flag-icons klass (https://flagicons.lipis.dev/) — sama väljanimi mis backendis.
export const UI_LANGUAGES = [
  { languageCode: 'et', flagIconCode: 'fi-ee' },
  { languageCode: 'en', flagIconCode: 'fi-gb' },
]
const SUPPORTED_LOCALES = UI_LANGUAGES.map((uiLanguage) => uiLanguage.languageCode)

export function getSavedLocale() {
  try {
    const savedLocale = localStorage.getItem(LOCALE_STORAGE_KEY)
    return SUPPORTED_LOCALES.includes(savedLocale) ? savedLocale : 'et'
  } catch {
    return 'et'
  }
}

export default createI18n({
  legacy: false,
  globalInjection: true,
  locale: getSavedLocale(),
  fallbackLocale: 'et',
  messages: { et, en },
})
