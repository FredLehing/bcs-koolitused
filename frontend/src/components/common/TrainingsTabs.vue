<script>
import NavTabs from '@/components/common/NavTabs.vue'

// Avalike koolitusevaadete vahelehed (samad lingid mis navbari menüüs "Koolitused")
export default {
  name: 'TrainingsTabs',
  components: { NavTabs },
  computed: {
    tabs() {
      return [
        { routeName: 'trainingsRoute', label: this.$t('navbar.ourTrainings') },
        { routeName: 'coursesRoute', label: this.$t('navbar.coursesCalendar') },
      ]
    },
  },
}
</script>

<template>
  <NavTabs :tabs="tabs" />
</template>
