<script>
import NavTabs from '@/components/common/NavTabs.vue'

// Admini nimekirjavaadete vahelehed (samad lingid ja järjekord mis navbari menüüs "Admin").
// Prototüüp (haru alternative-frontend-design): ainult uues stiilis vaated.
export default {
  name: 'AdminTabs',
  components: { NavTabs },
  computed: {
    tabs() {
      return [
        { routeName: 'adminFeedbacksRoute', label: this.$t('navbar.manageFeedbacks') },
        { routeName: 'adminAllCoursesRoute', label: this.$t('navbar.manageCourses') },
      ]
    },
  },
}
</script>

<template>
  <NavTabs :tabs="tabs" />
</template>
