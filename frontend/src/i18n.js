import { createI18n } from 'vue-i18n'
import et from '@/locales/et.json'
import en from '@/locales/en.json'

// Kasutajaliidese keel salvestatakse localStorage'isse (sama võti, mida kasutab sisukeel contentLang)
export const LOCALE_STORAGE_KEY = 'contentLang'
const SUPPORTED_LOCALES = ['et', 'en']

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
