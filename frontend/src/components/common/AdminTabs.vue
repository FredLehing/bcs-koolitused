<script>
// Admini nimekirjavaadete vahelehed (samad lingid ja järjekord mis navbari menüüs "Admin").
// Aktiivne vaheleht tuleb praegusest marsruudist.
export default {
  name: 'AdminTabs',
  computed: {
    tabs() {
      return [
        { routeName: 'adminEnquiriesRoute', label: this.$t('navbar.manageEnquiries') },
        { routeName: 'adminRegistrationsRoute', label: this.$t('navbar.manageRegistrations') },
        { routeName: 'adminTrainingsRoute', label: this.$t('navbar.manageTrainings') },
        { routeName: 'adminAllCoursesRoute', label: this.$t('navbar.manageCourses') },
        { routeName: 'adminLecturersRoute', label: this.$t('navbar.manageLecturers') },
        { routeName: 'adminRoomsRoute', label: this.$t('navbar.manageRooms') },
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
