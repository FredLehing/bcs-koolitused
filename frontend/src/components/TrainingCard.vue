<script>
import { PhCurrencyEur, PhStar } from '@phosphor-icons/vue'
import FlagIcon from '@/components/common/FlagIcon.vue'
import EditTrainingLink from '@/components/common/EditTrainingLink.vue'

export default {
  name: 'TrainingCard',
  components: { EditTrainingLink, FlagIcon, PhCurrencyEur, PhStar },
  props: {
    training: Object,
  },
  computed: {
    fundingTypeNames() {
      return (this.training.fundingTypes ?? []).map((f) => f.fundingTypeName).join(', ')
    },
  },
}
</script>

<template>
  <div class="card mb-3" :class="{ 'bg-warning-subtle': training.isPromoted }">
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-9">
          <h2 class="h5 d-flex align-items-center gap-2 mb-1">
            <PhStar
              v-if="training.isPromoted"
              :size="20"
              weight="fill"
              class="text-warning flex-shrink-0"
              :aria-label="$t('courses.promoted')"
            />
            {{ training.title }}
            <EditTrainingLink
              :training-id="training.trainingId"
              :training-translation-id="training.trainingTranslationId"
              class="flex-shrink-0"
            />
          </h2>
          <p class="mb-2">{{ training.shortDescription }}</p>
          <div class="d-flex flex-wrap align-items-center gap-2 small">
            <span v-if="training.categoryName" class="badge text-bg-primary">
              {{ training.categoryName }}
            </span>
            <span
              v-if="fundingTypeNames"
              class="d-inline-flex align-items-center gap-1 text-success"
            >
              <PhCurrencyEur :size="14" weight="bold" />
              {{ fundingTypeNames }}
            </span>
          </div>
        </div>
        <div class="col-md-3 d-flex flex-column align-items-md-end justify-content-between gap-2">
          <FlagIcon
            :flag-icon-code="training.trainingLanguageFlagIconCode"
            :title="$t('trainingCard.language')"
            class="fs-4 align-self-start align-self-md-end"
          />
          <span
            v-if="training.isOrderable"
            class="badge text-bg-success align-self-start align-self-md-end"
          >
            {{ $t('trainingCard.orderable') }}
          </span>
          <RouterLink
            :to="{
              name: 'trainingRoute',
              query: { returnTo: $route.fullPath, trainingId: training.trainingId },
            }"
            class="btn btn-primary"
          >
            {{ $t('trainingCard.viewDetails') }}
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>
