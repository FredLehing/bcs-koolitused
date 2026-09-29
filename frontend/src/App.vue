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
            <!-- Keelevalik: kasutajaliidese keeled tulevad store'ist (uiLanguages) -->
            <div class="d-flex align-items-center gap-1">
              <button
                v-for="uiLanguage in languageStore.uiLanguages"
                :key="uiLanguage.languageCode"
                @click="languageStore.setContentLang(uiLanguage.languageCode)"
                :class="{
                  'border-primary': languageStore.contentLang === uiLanguage.languageCode,
                }"
                :title="uiLanguage.languageCode.toUpperCase()"
                class="btn btn-sm btn-light border"
                type="button"
              >
                <FlagIcon :flag-icon-code="uiLanguage.flagIconCode" />
              </button>
            </div>
            <button
              v-if="userIsLoggedIn"
              @click="isLogoutModalOpen = true"
              class="btn btn-outline-secondary btn-sm"
              type="button"
            >
              {{ $t('navbar.logout') }}
            </button>
            <RouterLink v-else class="btn btn-outline-secondary btn-sm" to="/login">
              {{ $t('navbar.login') }}
            </RouterLink>
            <a v-if="!userIsLoggedIn" class="btn btn-outline-secondary btn-sm" href="#">{{
              $t('navbar.register')
            }}</a>
          </div>
        </div>
      </div>
    </nav>

    <ConfirmModal
      :is-open="isLogoutModalOpen"
      :title="$t('navbar.logoutModal.title')"
      :message="$t('navbar.logoutModal.message')"
      :confirm-label="$t('navbar.logout')"
      @event-confirmed="logout"
      @event-modal-closed="isLogoutModalOpen = false"
    />

    <RouterView />

    <FooterComponent />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import FooterComponent from '@/components/FooterComponent.vue'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'
import FlagIcon from '@/components/common/FlagIcon.vue'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import { useLanguageStore } from '@/stores/languageStore.js'

const route = useRoute()
const languageStore = useLanguageStore()

const userIsLoggedIn = ref(false)
const userIsAdmin = ref(false)
const isLogoutModalOpen = ref(false)

// sessionStorage ei ole reaktiivne — seisund loetakse uuesti iga marsruudi muutusel
// (nt pärast sisselogimist) ja väljalogimisel (avalehel olles marsruut ei pruugi muutuda)
function refreshSessionState() {
  userIsLoggedIn.value = SessionStorageService.userIsLoggedIn()
  userIsAdmin.value = SessionStorageService.userIsAdmin()
}

watch(() => route.fullPath, refreshSessionState, { immediate: true })

function logout() {
  isLogoutModalOpen.value = false
  SessionStorageService.clearSession()
  refreshSessionState()
  NavigationService.navigateToHomeView()
}
</script>
