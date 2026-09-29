<script>
import { PhCurrencyEur, PhShootingStar } from '@phosphor-icons/vue'
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'

export default {
  name: 'TrainingCard',
  components: { PhCurrencyEur, PhShootingStar },
  props: {
    training: Object,
  },
  computed: {
    ...mapState(useLanguageStore, ['getFlagClass']),
    fundingTypeNames() {
      return (this.training.fundingTypes ?? []).map((f) => f.fundingTypeName).join(', ')
    },
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
        <div v-if="fundingTypeNames" class="mt-2 d-flex align-items-center gap-1 fs-6 text-success">
          <span
            class="d-inline-flex align-items-center justify-content-center rounded-circle bg-success text-white p-1"
          >
            <PhCurrencyEur :size="14" weight="bold" />
          </span>
          <span>{{ fundingTypeNames }}</span>
        </div>
      </div>
      <div class="d-flex align-items-center flex-column gap-3 fs-5">
        <div class="d-flex flex-column align-items-center">
          <small class="text-body-secondary fs-6">{{ $t('trainingCard.language') }}</small>
          <span class="fi fs-3" :class="getFlagClass(training.trainingLanguageCode)"></span>
        </div>
        <span v-if="training.isOrderable" class="badge text-bg-success">{{
          $t('trainingCard.orderable')
        }}</span>
        <RouterLink to="/training" class="btn btn-primary">{{
          $t('trainingCard.viewDetails')
        }}</RouterLink>
      </div>
    </div>
  </div>
</template>
