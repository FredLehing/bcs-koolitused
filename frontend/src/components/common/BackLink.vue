<script>
import { PhArrowLeft } from '@phosphor-icons/vue'
import NavigationService from '@/services/NavigationService.js'

// Sama tagasilink detailides ja vormides. Otselink saab vaate enda mõistliku varusihtkoha.
export default {
  name: 'BackLink',
  components: { PhArrowLeft },
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
  <RouterLink
    :to="destination"
    class="mb-4 inline-flex min-h-11 items-center gap-1.5 self-start font-semibold"
  >
    <PhArrowLeft :size="18" weight="bold" />
    {{ $t('navigation.back') }}
  </RouterLink>
</template>
