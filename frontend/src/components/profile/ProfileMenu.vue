<script>
// Osaleja profiilivaadete ühine menüü (vasakul); kitsal ekraanil kaardi kohal horisontaalselt.
// Adminil seda menüüd pole — tal on ainult parooli vaade.
const PROFILE_MENU_ITEMS = [
  { routeName: 'participantDetailsRoute', labelKey: 'navbar.participantDetails' },
  // Tagasiside vorm on "Minu koolitused" alamvaade — menüüpunkt on ka seal aktiivne
  {
    routeName: 'participantCoursesRoute',
    labelKey: 'navbar.participantCourses',
    activeRouteNames: ['participantFeedbackFormRoute'],
  },
  { routeName: 'participantCertificatesRoute', labelKey: 'navbar.participantCertificates' },
  { routeName: 'changePasswordRoute', labelKey: 'navbar.changePassword' },
]

export default {
  name: 'ProfileMenu',
  computed: {
    profileMenuItems() {
      return PROFILE_MENU_ITEMS
    },
  },
}
</script>

<template>
  <nav :aria-label="$t('navbar.participantDetails')">
    <ul
      class="mb-6 flex gap-6 overflow-x-auto border-b border-line [scrollbar-width:thin] lg:mb-0 lg:flex-col lg:gap-1 lg:border-b-0"
    >
      <li
        v-for="profileMenuItem in profileMenuItems"
        :key="profileMenuItem.routeName"
        class="shrink-0"
      >
        <RouterLink
          :to="{ name: profileMenuItem.routeName }"
          :class="
            $route.name === profileMenuItem.routeName ||
            profileMenuItem.activeRouteNames?.includes($route.name)
              ? 'active border-brand-600 text-brand-600 lg:bg-brand-50'
              : 'border-transparent text-muted hover:text-ink lg:hover:bg-surface'
          "
          :aria-current="$route.name === profileMenuItem.routeName ? 'page' : undefined"
          class="-mb-px block min-h-11 border-b-[3px] px-0.5 pt-2 pb-3 font-semibold whitespace-nowrap lg:mb-0 lg:rounded-lg lg:border-b-0 lg:border-l-[3px] lg:px-3 lg:py-2.5"
        >
          {{ $t(profileMenuItem.labelKey) }}
        </RouterLink>
      </li>
    </ul>
  </nav>
</template>
