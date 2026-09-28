<template>
  <div class="d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-light bg-white mb-3">
      <div class="container">
        <RouterLink class="navbar-brand" to="/">
          <img src="@/assets/bcs-koolitus.svg" alt="BCS koolituse logo" height="68" />
        </RouterLink>
        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navMenu"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navMenu">
          <div class="navbar-nav gap-4 mx-auto bg-primary bg-opacity-50 rounded-pill px-3">
            <RouterLink class="nav-link" to="/trainings">{{ $t('navbar.trainings') }}</RouterLink>
            <a class="nav-link" href="#">{{ $t('navbar.services') }}</a>

            <div class="dropdown">
              <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                {{ $t('navbar.company') }}
              </a>
              <div class="dropdown-menu bg-primary bg-opacity-50">
                <a class="nav-link" href="#">{{ $t('navbar.lecturers') }}</a>
              </div>
            </div>

            <a class="nav-link" href="#">{{ $t('navbar.blog') }}</a>
            <a class="nav-link" href="#">{{ $t('navbar.contact') }}</a>
            <a class="nav-link" href="#">{{ $t('navbar.feedback') }}</a>

            <div v-if="userIsAdmin" class="dropdown">
              <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                {{ $t('navbar.admin') }}
              </a>
              <div class="dropdown-menu bg-primary bg-opacity-50">
                <RouterLink class="nav-link" :to="{ name: 'trainingFormRoute' }">
                  {{ $t('navbar.addTraining') }}
                </RouterLink>
              </div>
            </div>
          </div>

          <div class="d-flex align-items-center gap-3">
            <!-- Keelevalik: keeled tulevad store'ist (contentLanguages) -->
            <div class="d-flex align-items-center gap-1">
              <button
                v-for="contentLanguage in languageStore.contentLanguages"
                :key="contentLanguage.languageCode"
                @click="changeLocale(contentLanguage.languageCode)"
                :class="{ 'border-primary': locale === contentLanguage.languageCode }"
                :title="contentLanguage.languageCode.toUpperCase()"
                class="btn btn-sm btn-light border"
                type="button"
              >
                <span class="fi" :class="contentLanguage.languageFlag"></span>
              </button>
            </div>
            <RouterLink class="btn btn-outline-secondary btn-sm" to="/login">
              {{ $t('navbar.login') }}
            </RouterLink>
            <a class="btn btn-outline-secondary btn-sm" href="#">{{ $t('navbar.register') }}</a>
          </div>
        </div>
      </div>
    </nav>

    <RouterView />

    <FooterComponent />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import FooterComponent from '@/components/FooterComponent.vue'
import SessionStorageService from '@/services/SessionStorageService.js'
import { useLanguageStore } from '@/stores/languageStore.js'
import { LOCALE_STORAGE_KEY } from '@/i18n.js'

const route = useRoute()
const { locale } = useI18n()
const languageStore = useLanguageStore()

// sessionStorage ei ole reaktiivne — kontroll tehakse uuesti iga marsruudi muutusel (nt pärast sisselogimist)
const userIsAdmin = computed(() => route.fullPath !== '' && SessionStorageService.userIsAdmin())

function changeLocale(languageCode) {
  locale.value = languageCode
  try {
    localStorage.setItem(LOCALE_STORAGE_KEY, languageCode)
  } catch {
    // localStorage võib olla keelatud — keel kehtib siis ainult selle seansi ajal
  }
}
</script>
