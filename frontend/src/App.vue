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
              <div @click="closeNavbarDropdowns" class="dropdown-menu bg-bcs-primary">
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
              <div @click="closeNavbarDropdowns" class="dropdown-menu bg-bcs-primary">
                <!-- Rühmad: igapäevane töö (päringud, registreerumised) | koolitused | koolitajad ja ruumid | kontod.
                     "Lisa uus" nupud on nimekirja vaadetes -->
                <RouterLink class="nav-link" :to="{ name: 'adminEnquiriesRoute' }">
                  {{ $t('navbar.manageEnquiries') }}
                </RouterLink>
                <RouterLink class="nav-link" :to="{ name: 'adminRegistrationsRoute' }">
                  {{ $t('navbar.manageRegistrations') }}
                </RouterLink>
                <hr class="dropdown-divider" />
                <RouterLink class="nav-link" :to="{ name: 'adminTrainingsRoute' }">
                  {{ $t('navbar.manageTrainings') }}
                </RouterLink>
                <RouterLink class="nav-link" :to="{ name: 'adminAllCoursesRoute' }">
                  {{ $t('navbar.manageCourses') }}
                </RouterLink>
                <hr class="dropdown-divider" />
                <RouterLink class="nav-link" :to="{ name: 'adminLecturersRoute' }">
                  {{ $t('navbar.manageLecturers') }}
                </RouterLink>
                <RouterLink class="nav-link" :to="{ name: 'adminRoomsRoute' }">
                  {{ $t('navbar.manageRooms') }}
                </RouterLink>
                <hr class="dropdown-divider" />
                <RouterLink class="nav-link" :to="{ name: 'adminUsersRoute' }">
                  {{ $t('navbar.manageUsers') }}
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
            <!-- Minu profiil: osalejal neli vaadet, adminil ainult parool -->
            <div v-if="userIsLoggedIn" class="dropdown">
              <!-- Ainult ikoon (nimi title/aria-label-is), et navbar ei läheks kitsaks -->
              <button
                :class="{ 'border-primary': isProfileRouteActive }"
                :title="$t('navbar.profile')"
                :aria-label="$t('navbar.profile')"
                class="btn btn-light border btn-sm dropdown-toggle d-inline-flex align-items-center gap-1"
                type="button"
                data-bs-toggle="dropdown"
              >
                <PhUserCircle :size="20" />
              </button>
              <div @click="closeNavbarDropdowns" class="dropdown-menu dropdown-menu-end">
                <h6 class="dropdown-header">{{ $t('navbar.profile') }}</h6>
                <template v-if="!userIsAdmin">
                  <RouterLink class="dropdown-item" :to="{ name: 'participantDetailsRoute' }">
                    {{ $t('navbar.participantDetails') }}
                  </RouterLink>
                  <RouterLink class="dropdown-item" :to="{ name: 'participantCoursesRoute' }">
                    {{ $t('navbar.participantCourses') }}
                  </RouterLink>
                  <RouterLink class="dropdown-item" :to="{ name: 'participantCertificatesRoute' }">
                    {{ $t('navbar.participantCertificates') }}
                  </RouterLink>
                </template>
                <RouterLink class="dropdown-item" :to="{ name: 'changePasswordRoute' }">
                  {{ $t('navbar.changePassword') }}
                </RouterLink>
              </div>
            </div>
            <!-- Läbipaistmatu taust (btn-light): klaasja navbari all võib olla tume pilt -->
            <button
              v-if="userIsLoggedIn"
              @click="isLogoutModalOpen = true"
              class="btn btn-light border btn-sm"
              type="button"
            >
              {{ $t('navbar.logout') }}
            </button>
            <RouterLink v-else class="btn btn-light border btn-sm" to="/login">
              {{ $t('navbar.login') }}
            </RouterLink>
            <RouterLink
              v-if="!userIsLoggedIn"
              class="btn btn-light border btn-sm"
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
import { computed, ref, watch } from 'vue'
import { PhUserCircle } from '@phosphor-icons/vue'
import { Dropdown } from 'bootstrap'
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

const PROFILE_ROUTE_NAMES = [
  'participantDetailsRoute',
  'participantCoursesRoute',
  'participantCertificatesRoute',
  'changePasswordRoute',
]
const isProfileRouteActive = computed(() => PROFILE_ROUTE_NAMES.includes(route.name))

// sessionStorage ei ole reaktiivne — seisund loetakse uuesti iga marsruudi muutusel
// (nt pärast sisselogimist) ja väljalogimisel (avalehel olles marsruut ei pruugi muutuda)
function refreshSessionState() {
  userIsLoggedIn.value = SessionStorageService.userIsLoggedIn()
  userIsAdmin.value = SessionStorageService.userIsAdmin()
}

// Navbari rippmenüü suletakse ise: menüüpunktile klõpsates (ka siis, kui see on juba avatud leht)
// ja igal marsruudi muutusel. Bootstrapi enda sulgemine sõltub nupu .show klassist, mille Vue
// :class-iga üle kirjutab; hide() vaatab menüü enda .show klassi.
function closeNavbarDropdowns() {
  document.querySelectorAll('.navbar .dropdown-menu.show').forEach((dropdownMenu) => {
    const dropdownToggle = dropdownMenu.parentElement.querySelector('[data-bs-toggle="dropdown"]')
    Dropdown.getOrCreateInstance(dropdownToggle).hide()
  })
}

watch(
  () => route.fullPath,
  () => {
    refreshSessionState()
    closeNavbarDropdowns()
  },
  { immediate: true },
)

function logout() {
  isLogoutModalOpen.value = false
  SessionStorageService.clearSession()
  refreshSessionState()
  NavigationService.navigateToHomeView()
}
</script>
