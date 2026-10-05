Võib <script>
import NavigationService from '@/services/NavigationService.js'

// Sama tagasilink detailides ja vormides. Otselink saab vaate enda mõistliku varusihtkoha.
export default {
  name: 'BackLink',
  props: {
    fallback: { type: [String, Object], required: true },
  },
  computed: {
    destination() {
      return (
        NavigationService.getReturnTo(this.$route.query.returnTo, this.$route.fullPath) ||
        this.fallback
      )
    },
  },
}
</script>

<template>
  <RouterLink :to="destination" class="d-inline-block mb-3"
    >← {{ $t('navigation.back') }}</RouterLink
  >
</template>
