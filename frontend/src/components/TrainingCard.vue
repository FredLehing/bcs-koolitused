<script>
import { PhShootingStar } from '@phosphor-icons/vue'
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'

export default {
  name: 'TrainingCard',
  components: { PhShootingStar },
  props: {
    training: Object,
  },
  computed: {
    ...mapState(useLanguageStore, ['getFlagClass']),
  },
}
</script>

<template>
  <div class="card mb-4" :class="{ 'bg-warning-subtle': training.isPromoted }">
    <h5 class="card-header fs-3 d-flex justify-content-between align-items-center">
      {{ training.title }}
      <PhShootingStar v-if="training.isPromoted" :size="32" />
    </h5>
    <div class="card-body d-flex justify-content-between fs-5">
      <div>
        <p class="card-text">{{ training.shortDescription }}</p>
        <span class="badge text-bg-primary">{{ training.categoryName }}</span>
        <div class="mt-2 d-flex gap-2 fs-5">
          <span
            v-for="fundingType in training.fundingTypes"
            :key="fundingType.fundingTypeId"
            class="badge text-bg-primary"
            >{{ fundingType.fundingTypeName }}</span
          >
        </div>
      </div>
      <div class="d-flex align-items-center flex-column gap-3 fs-5">
        <span class="fi fs-3" :class="getFlagClass(training.trainingLanguageCode)"></span>
        <span v-if="training.isOrderable" class="badge text-bg-success">Tellitav</span>
        <RouterLink to="/training" class="btn btn-primary">Vaata lähemalt</RouterLink>
      </div>
    </div>
  </div>
</template>
