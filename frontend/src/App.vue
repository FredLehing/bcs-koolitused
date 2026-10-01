<template>
  <div class="d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-light navbar-glass mb-3 shadow sticky-top">
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
          <div class="navbar-nav gap-4 mx-auto bg-bcs-primary rounded-pill px-3">
            <div class="dropdown">
              <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                {{ $t('navbar.trainings') }}
              </a>
              <div class="dropdown-menu bg-bcs-primary">
                <RouterLink class="nav-link" :to="{ name: 'trainingsRoute' }">
                  {{ $t('navbar.ourTrainings') }}
                </RouterLink>
                <RouterLink class="nav-link" :to="{ name: 'coursesRoute' }">
                  {{ $t('navbar.coursesCalendar') }}
                </RouterLink>
              </div>
            </div>
            <RouterLink class="nav-link" :to="{ name: 'lecturersRoute' }">
              {{ $t('navbar.ourLecturers') }}
            </RouterLink>
            <a class="nav-link" href="#">{{ $t('navbar.services') }}</a>

            <a class="nav-link" href="#">{{ $t('navbar.company') }}</a>

            <a class="nav-link" href="#">{{ $t('navbar.blog') }}</a>
            <a class="nav-link" href="#">{{ $t('navbar.contact') }}</a>
            <a class="nav-link" href="#">{{ $t('navbar.feedback') }}</a>

            <div v-if="userIsAdmin" class="dropdown">
              <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                {{ $t('navbar.admin') }}
              </a>
              <div class="dropdown-menu bg-bcs-primary">
                <RouterLink class="nav-link" :to="{ name: 'trainingFormRoute' }">
                  {{ $t('navbar.addTraining') }}
                </RouterLink>
                <RouterLink class="nav-link" :to="{ name: 'adminTrainingsRoute' }">
                  {{ $t('navbar.manageTrainings') }}
                </RouterLink>
                <RouterLink class="nav-link" :to="{ name: 'adminAllCoursesRoute' }">
                  {{ $t('navbar.manageCourses') }}
                </RouterLink>
                <hr class="dropdown-divider" />
                <RouterLink class="nav-link" :to="{ name: 'lecturerFormRoute' }">
                  {{ $t('navbar.addLecturer') }}
                </RouterLink>
                <RouterLink class="nav-link" :to="{ name: 'adminLecturersRoute' }">
                  {{ $t('navbar.manageLecturers') }}
                </RouterLink>
                <hr class="dropdown-divider" />
                <RouterLink class="nav-link" :to="{ name: 'adminRoomsRoute' }">
                  {{ $t('navbar.manageRooms') }}
                </RouterLink>
                <hr class="dropdown-divider" />
                <RouterLink class="nav-link" :to="{ name: 'adminEnquiriesRoute' }">
                  {{ $t('navbar.manageEnquiries') }}
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
            <RouterLink
              v-if="!userIsLoggedIn"
              class="btn btn-outline-secondary btn-sm"
              :to="{ name: 'signupRoute' }"
            >
              {{ $t('navbar.signup') }}
            </RouterLink>
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
