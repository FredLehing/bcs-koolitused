<template>
  <div class="flex min-h-screen flex-col">
    <header class="sticky top-0 z-30 border-b border-line bg-white/95 backdrop-blur">
      <div class="mx-auto flex w-full max-w-6xl items-center gap-6 px-4 py-3 sm:px-6">
        <RouterLink to="/" class="flex shrink-0 items-center">
          <img src="@/assets/bcs-koolitus.svg" alt="BCS Koolitus" class="h-10 w-auto sm:h-12" />
        </RouterLink>

        <!-- Desktop menüü -->
        <nav class="hidden flex-1 items-center gap-1 lg:flex" :aria-label="$t('navbar.menu')">
          <RouterLink
            v-for="navLink in publicNavLinks"
            :key="navLink.label"
            :to="navLink.to"
            class="rounded-lg px-3 py-2 font-medium text-ink hover:bg-brand-50 hover:text-brand-700"
            active-class="bg-brand-50 text-brand-700!"
          >
            {{ navLink.label }}
          </RouterLink>
          <!-- Sisuta lingid ainult väga laial ekraanil, et menüü mahuks ühele reale -->
          <a
            v-for="placeholderLabel in placeholderNavLabels"
            :key="placeholderLabel"
            href="#"
            class="hidden rounded-lg px-3 py-2 font-medium text-ink hover:bg-brand-50 hover:text-brand-700 xl:block"
          >
            {{ placeholderLabel }}
          </a>

          <div v-if="userIsAdmin" ref="adminMenu" class="relative">
            <button
              @click="toggleMenu('admin')"
              :aria-expanded="openMenu === 'admin'"
              class="inline-flex items-center gap-1 rounded-lg px-3 py-2 font-medium text-ink hover:bg-brand-50 hover:text-brand-700"
              type="button"
            >
              {{ $t('navbar.admin') }}
              <PhCaretDown :size="14" weight="bold" />
            </button>
            <div
              v-if="openMenu === 'admin'"
              class="absolute left-0 mt-2 w-64 overflow-hidden rounded-xl border border-line bg-white py-2 shadow-xl shadow-navy/10"
            >
              <!-- Rühmad: igapäevane töö | koolitused | koolitajad ja ruumid | kontod -->
              <div
                v-for="(navGroup, navGroupIndex) in adminNavGroups"
                :key="navGroupIndex"
                :class="{ 'mt-2 border-t border-line pt-2': navGroupIndex > 0 }"
              >
                <RouterLink
                  v-for="navLink in navGroup"
                  :key="navLink.label"
                  :to="navLink.to"
                  class="block px-4 py-2.5 text-ink hover:bg-brand-50 hover:text-brand-700"
                >
                  {{ navLink.label }}
                </RouterLink>
              </div>
            </div>
          </div>
        </nav>

        <div class="ml-auto flex items-center gap-2">
          <!-- Keelevalik: kasutajaliidese keeled tulevad store'ist (uiLanguages) -->
          <div class="flex overflow-hidden rounded-lg border border-line">
            <button
              v-for="uiLanguage in languageStore.uiLanguages"
              :key="uiLanguage.languageCode"
              @click="languageStore.setContentLang(uiLanguage.languageCode)"
              :aria-pressed="languageStore.contentLang === uiLanguage.languageCode"
              :class="
                languageStore.contentLang === uiLanguage.languageCode
                  ? 'bg-navy text-white'
                  : 'bg-white text-muted hover:bg-brand-50'
              "
              class="min-h-9 cursor-pointer px-2.5 text-sm font-bold uppercase"
              type="button"
            >
              {{ uiLanguage.languageCode }}
            </button>
          </div>
          <!-- Minu profiil: osalejal neli vaadet, adminil ainult parool -->
          <div v-if="userIsLoggedIn" ref="profileMenu" class="relative hidden sm:block">
            <button
              @click="toggleMenu('profile')"
              :aria-expanded="openMenu === 'profile'"
              :title="$t('navbar.profile')"
              :aria-label="$t('navbar.profile')"
              :class="{ 'border-brand-600 bg-brand-50': isProfileRouteActive }"
              class="btn btn-outline-secondary btn-sm"
              type="button"
            >
              <PhUserCircle :size="20" />
              <PhCaretDown :size="12" weight="bold" />
            </button>
            <div
              v-if="openMenu === 'profile'"
              class="absolute right-0 mt-2 w-60 overflow-hidden rounded-xl border border-line bg-white py-2 shadow-xl shadow-navy/10"
            >
              <div class="px-4 pt-1 pb-2 text-xs font-bold tracking-wide text-muted uppercase">
                {{ $t('navbar.profile') }}
              </div>
              <RouterLink
                v-for="navLink in profileNavLinks"
                :key="navLink.label"
                :to="navLink.to"
                class="block px-4 py-2.5 text-ink hover:bg-brand-50 hover:text-brand-700"
              >
                {{ navLink.label }}
              </RouterLink>
              <div class="mt-2 border-t border-line pt-2">
                <button
                  @click="isLogoutModalOpen = true"
                  class="block w-full cursor-pointer px-4 py-2.5 text-left text-ink hover:bg-brand-50 hover:text-brand-700"
                  type="button"
                >
                  {{ $t('navbar.logout') }}
                </button>
              </div>
            </div>
          </div>
          <template v-else>
            <RouterLink to="/login" class="btn btn-outline-primary btn-sm hidden sm:inline-flex">
              {{ $t('navbar.login') }}
            </RouterLink>
            <RouterLink
              :to="{ name: 'signupRoute' }"
              class="btn btn-primary btn-sm hidden sm:inline-flex"
            >
              {{ $t('navbar.signup') }}
            </RouterLink>
          </template>

          <!-- Mobiilimenüü nupp -->
          <button
            @click="toggleMenu('mobile')"
            :aria-expanded="openMenu === 'mobile'"
            :aria-label="$t('navbar.menu')"
            class="btn btn-outline-secondary btn-sm btn-icon lg:hidden"
            type="button"
          >
            <PhX v-if="openMenu === 'mobile'" :size="20" />
            <PhList v-else :size="20" />
          </button>
        </div>
      </div>

      <!-- Mobiilimenüü paneel -->
      <nav
        v-if="openMenu === 'mobile'"
        class="border-t border-line bg-white px-4 pt-2 pb-4 lg:hidden"
        :aria-label="$t('navbar.menu')"
      >
        <RouterLink
          v-for="navLink in publicNavLinks"
          :key="navLink.label"
          :to="navLink.to"
          class="flex min-h-12 items-center rounded-lg px-3 font-medium text-ink hover:bg-brand-50"
        >
          {{ navLink.label }}
        </RouterLink>
        <a
          v-for="placeholderLabel in placeholderNavLabels"
          :key="placeholderLabel"
          href="#"
          class="flex min-h-12 items-center rounded-lg px-3 font-medium text-ink hover:bg-brand-50"
        >
          {{ placeholderLabel }}
        </a>
        <template v-if="userIsAdmin">
          <div class="mt-2 px-3 pt-3 pb-1 text-xs font-bold tracking-wide text-muted uppercase">
            {{ $t('navbar.admin') }}
          </div>
          <RouterLink
            v-for="navLink in adminNavGroups.flat()"
            :key="navLink.label"
            :to="navLink.to"
            class="flex min-h-12 items-center rounded-lg px-3 font-medium text-ink hover:bg-brand-50"
          >
            {{ navLink.label }}
          </RouterLink>
        </template>
        <template v-if="userIsLoggedIn">
          <div class="mt-2 px-3 pt-3 pb-1 text-xs font-bold tracking-wide text-muted uppercase">
            {{ $t('navbar.profile') }}
          </div>
          <RouterLink
            v-for="navLink in profileNavLinks"
            :key="navLink.label"
            :to="navLink.to"
            class="flex min-h-12 items-center rounded-lg px-3 font-medium text-ink hover:bg-brand-50"
          >
            {{ navLink.label }}
          </RouterLink>
        </template>
        <div class="mt-3 flex flex-col gap-2 border-t border-line pt-3">
          <button
            v-if="userIsLoggedIn"
            @click="isLogoutModalOpen = true"
            class="btn btn-outline-secondary w-full"
            type="button"
          >
            {{ $t('navbar.logout') }}
          </button>
          <template v-else>
            <RouterLink to="/login" class="btn btn-primary w-full">
              {{ $t('navbar.login') }}
            </RouterLink>
            <RouterLink :to="{ name: 'signupRoute' }" class="btn btn-outline-primary w-full">
              {{ $t('navbar.signup') }}
            </RouterLink>
          </template>
        </div>
      </nav>
    </header>

    <ConfirmModal
      :is-open="isLogoutModalOpen"
      :title="$t('navbar.logoutModal.title')"
      :message="$t('navbar.logoutModal.message')"
      :confirm-label="$t('navbar.logout')"
      @event-confirmed="logout"
      @event-modal-closed="isLogoutModalOpen = false"
    />

    <main class="flex flex-1 flex-col">
      <RouterView />
    </main>

    <FooterComponent />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, useTemplateRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { PhCaretDown, PhList, PhUserCircle, PhX } from '@phosphor-icons/vue'
import { useRoute } from 'vue-router'
import FooterComponent from '@/components/FooterComponent.vue'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import { useLanguageStore } from '@/stores/languageStore.js'

const route = useRoute()
const { t } = useI18n()
const languageStore = useLanguageStore()

const userIsLoggedIn = ref(false)
const userIsAdmin = ref(false)
const isLogoutModalOpen = ref(false)
// Avatud menüü: 'admin' ja 'profile' (desktopi rippmenüüd), 'mobile' (mobiilimenüü paneel) või null
const openMenu = ref(null)
const adminMenu = useTemplateRef('adminMenu')
const profileMenu = useTemplateRef('profileMenu')

const PROFILE_ROUTE_NAMES = [
  'participantDetailsRoute',
  'participantCoursesRoute',
  'participantCertificatesRoute',
  'changePasswordRoute',
]
const isProfileRouteActive = computed(() => PROFILE_ROUTE_NAMES.includes(route.name))

const publicNavLinks = computed(() => [
  { label: t('navbar.ourTrainings'), to: { name: 'trainingsRoute' } },
  { label: t('navbar.coursesCalendar'), to: { name: 'coursesRoute' } },
  { label: t('navbar.ourLecturers'), to: { name: 'lecturersRoute' } },
])
const placeholderNavLabels = computed(() => [t('navbar.services'), t('navbar.contact')])
// "Lisa uus" nupud on nimekirja vaadetes
const adminNavGroups = computed(() => [
  [
    { label: t('navbar.manageEnquiries'), to: { name: 'adminEnquiriesRoute' } },
    { label: t('navbar.manageRegistrations'), to: { name: 'adminRegistrationsRoute' } },
    { label: t('navbar.manageFeedbacks'), to: { name: 'adminFeedbacksRoute' } },
  ],
  [
    { label: t('navbar.manageTrainings'), to: { name: 'adminTrainingsRoute' } },
    { label: t('navbar.manageCourses'), to: { name: 'adminAllCoursesRoute' } },
  ],
  [
    { label: t('navbar.manageLecturers'), to: { name: 'adminLecturersRoute' } },
    { label: t('navbar.manageRooms'), to: { name: 'adminRoomsRoute' } },
  ],
  [{ label: t('navbar.manageUsers'), to: { name: 'adminUsersRoute' } }],
])
const profileNavLinks = computed(() => {
  const changePasswordLink = {
    label: t('navbar.changePassword'),
    to: { name: 'changePasswordRoute' },
  }
  if (userIsAdmin.value) {
    return [changePasswordLink]
  }
  return [
    { label: t('navbar.participantDetails'), to: { name: 'participantDetailsRoute' } },
    { label: t('navbar.participantCourses'), to: { name: 'participantCoursesRoute' } },
    { label: t('navbar.participantCertificates'), to: { name: 'participantCertificatesRoute' } },
    changePasswordLink,
  ]
})

// sessionStorage ei ole reaktiivne — seisund loetakse uuesti iga marsruudi muutusel
// (nt pärast sisselogimist) ja väljalogimisel (avalehel olles marsruut ei pruugi muutuda)
function refreshSessionState() {
  userIsLoggedIn.value = SessionStorageService.userIsLoggedIn()
  userIsAdmin.value = SessionStorageService.userIsAdmin()
}

function toggleMenu(menuName) {
  openMenu.value = openMenu.value === menuName ? null : menuName
}

// Rippmenüü sulgub klikiga väljaspool seda
function handleDocumentClick(event) {
  const openDropdown = { admin: adminMenu.value, profile: profileMenu.value }[openMenu.value]
  if (openDropdown && !openDropdown.contains(event.target)) {
    openMenu.value = null
  }
}

function handleKeydown(event) {
  if (event.key === 'Escape') {
    openMenu.value = null
  }
}

watch(
  () => route.fullPath,
  () => {
    refreshSessionState()
    openMenu.value = null
  },
  { immediate: true },
)

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
  document.removeEventListener('keydown', handleKeydown)
})

function logout() {
  isLogoutModalOpen.value = false
  openMenu.value = null
  SessionStorageService.clearSession()
  refreshSessionState()
  NavigationService.navigateToHomeView()
}
</script>
