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
          <a
            v-for="placeholderLabel in placeholderNavLabels"
            :key="placeholderLabel"
            href="#"
            class="rounded-lg px-3 py-2 font-medium text-ink hover:bg-brand-50 hover:text-brand-700"
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
              <RouterLink
                v-for="navLink in adminNavLinks"
                :key="navLink.label"
                :to="navLink.to"
                class="block px-4 py-2.5 text-ink hover:bg-brand-50 hover:text-brand-700"
              >
                {{ navLink.label }}
              </RouterLink>
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
          <button
            v-if="userIsLoggedIn"
            @click="isLogoutModalOpen = true"
            class="btn btn-outline-secondary btn-sm hidden sm:inline-flex"
            type="button"
          >
            {{ $t('navbar.logout') }}
          </button>
          <RouterLink
            v-else
            to="/login"
            class="btn btn-outline-primary btn-sm hidden sm:inline-flex"
          >
            {{ $t('navbar.login') }}
          </RouterLink>

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
            v-for="navLink in adminNavLinks"
            :key="navLink.label"
            :to="navLink.to"
            class="flex min-h-12 items-center rounded-lg px-3 font-medium text-ink hover:bg-brand-50"
          >
            {{ navLink.label }}
          </RouterLink>
        </template>
        <div class="mt-3 border-t border-line pt-3 sm:hidden">
          <button
            v-if="userIsLoggedIn"
            @click="isLogoutModalOpen = true"
            class="btn btn-outline-secondary w-full"
            type="button"
          >
            {{ $t('navbar.logout') }}
          </button>
          <RouterLink v-else to="/login" class="btn btn-primary w-full">
            {{ $t('navbar.login') }}
          </RouterLink>
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
import { PhCaretDown, PhList, PhX } from '@phosphor-icons/vue'
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
// Avatud menüü: 'admin' (desktopi rippmenüü), 'mobile' (mobiilimenüü paneel) või null
const openMenu = ref(null)
const adminMenu = useTemplateRef('adminMenu')

// Prototüüp (haru alternative-frontend-design): menüüs on ainult uues stiilis vaated.
// Teised vaated töötavad otselingiga, aga on veel ümber kujundamata.
const publicNavLinks = computed(() => [
  { label: t('navbar.coursesCalendar'), to: { name: 'coursesRoute' } },
])
const placeholderNavLabels = computed(() => [t('navbar.services'), t('navbar.contact')])
const adminNavLinks = computed(() => [
  { label: t('navbar.manageFeedbacks'), to: { name: 'adminFeedbacksRoute' } },
  { label: t('navbar.manageCourses'), to: { name: 'adminAllCoursesRoute' } },
])

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
  if (openMenu.value === 'admin' && !adminMenu.value?.contains(event.target)) {
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
