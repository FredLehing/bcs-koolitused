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
  <nav class="nav nav-pills flex-row flex-lg-column gap-1 border rounded bg-body p-2">
    <RouterLink
      v-for="profileMenuItem in profileMenuItems"
      :key="profileMenuItem.routeName"
      :to="{ name: profileMenuItem.routeName }"
      active-class="active"
      :class="{ active: profileMenuItem.activeRouteNames?.includes($route.name) }"
      class="nav-link text-start"
    >
      {{ $t(profileMenuItem.labelKey) }}
    </RouterLink>
  </nav>
</template>
