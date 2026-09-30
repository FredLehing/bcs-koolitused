<script>
import { PhPencilSimple } from '@phosphor-icons/vue'
import SessionStorageService from '@/services/SessionStorageService.js'

// Muutmise ikoon, mis avab koolituse tõlke vormis (TrainingFormView, olek "update").
// Kuvatakse ainult adminile; vorm kontrollib rolli avamisel uuesti.
export default {
  name: 'EditTrainingLink',
  components: { PhPencilSimple },
  props: {
    trainingId: Number,
    trainingTranslationId: Number,
  },
  computed: {
    userIsAdmin() {
      return SessionStorageService.userIsAdmin()
    },
  },
}
</script>

<template>
  <RouterLink
    v-if="userIsAdmin"
    :to="{
      name: 'trainingFormRoute',
      query: { trainingId: trainingId, trainingTranslationId: trainingTranslationId },
    }"
    :title="$t('trainingCard.edit')"
    :aria-label="$t('trainingCard.edit')"
    class="btn btn-sm btn-outline-secondary d-inline-flex"
  >
    <PhPencilSimple :size="20" />
  </RouterLink>
</template>
