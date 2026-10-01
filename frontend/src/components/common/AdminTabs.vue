<script>
import NavTabs from '@/components/common/NavTabs.vue'

// Admini nimekirjavaadete vahelehed (samad lingid ja järjekord mis navbari menüüs "Admin")
export default {
  name: 'AdminTabs',
  components: { NavTabs },
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
  <NavTabs :tabs="tabs" />
</template>
