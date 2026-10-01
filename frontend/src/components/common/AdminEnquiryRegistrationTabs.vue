<script>
// Päringute ja registreerumiste vaadete vaheline lülitus (AdminEnquiriesView, AdminRegistrationsView).
// Aktiivne vaheleht tuleb praegusest marsruudist.
export default {
  name: 'AdminEnquiryRegistrationTabs',
  computed: {
    tabs() {
      return [
        { routeName: 'adminEnquiriesRoute', label: this.$t('navbar.manageEnquiries') },
        { routeName: 'adminRegistrationsRoute', label: this.$t('navbar.manageRegistrations') },
      ]
    },
  },
}
</script>

<template>
  <ul class="nav nav-tabs mb-3">
    <li v-for="tab in tabs" :key="tab.routeName" class="nav-item">
      <RouterLink
        :to="{ name: tab.routeName }"
        :class="{ active: $route.name === tab.routeName }"
        :aria-current="$route.name === tab.routeName ? 'page' : undefined"
        class="nav-link"
      >
        {{ tab.label }}
      </RouterLink>
    </li>
  </ul>
</template>
