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
  <article
    :class="training.isPromoted ? 'border-brand-200 bg-brand-50' : 'border-line bg-white'"
    class="flex flex-col gap-4 rounded-2xl border p-4 transition-shadow hover:shadow-lg hover:shadow-brand-600/10 sm:p-5 md:flex-row md:items-center md:gap-6"
  >
    <div class="flex min-w-0 flex-1 flex-col gap-2">
      <div class="flex flex-wrap items-center gap-2">
        <h2 class="text-lg font-bold sm:text-xl">{{ training.title }}</h2>
        <span v-if="training.isPromoted" class="badge text-bg-primary">
          <PhStar :size="13" weight="fill" />
          {{ $t('courses.promoted') }}
        </span>
        <EditTrainingLink
          :training-id="training.trainingId"
          :training-translation-id="training.trainingTranslationId"
          class="shrink-0"
        />
      </div>
      <p class="text-muted">{{ training.shortDescription }}</p>
      <div class="flex flex-wrap items-center gap-2 text-sm">
        <span
          v-if="training.categoryName"
          class="rounded-md bg-brand-100 px-2.5 py-0.5 font-semibold text-brand-700"
        >
          {{ training.categoryName }}
        </span>
        <span
          v-if="fundingTypeNames"
          class="inline-flex items-center gap-1 font-medium text-emerald-800"
        >
          <PhCurrencyEur :size="14" weight="bold" />
          {{ fundingTypeNames }}
        </span>
      </div>
    </div>

    <div
      class="flex items-center justify-between gap-4 border-t border-line pt-4 md:shrink-0 md:flex-col md:items-end md:border-0 md:pt-0"
    >
      <div class="flex items-center gap-3">
        <FlagIcon
          :flag-icon-code="training.trainingLanguageFlagIconCode"
          :title="$t('trainingCard.language')"
          class="text-xl"
        />
        <span v-if="training.isOrderable" class="badge text-bg-success">
          {{ $t('trainingCard.orderable') }}
        </span>
      </div>
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
  </article>
</template>
