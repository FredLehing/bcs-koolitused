<script>
import FlagIcon from '@/components/common/FlagIcon.vue'

// Koolituse kaart (AdminTrainingDto): pealkiri, staatus ja faktid. Koolitajad kuvatakse ainult nimedena.
// Tegevuslingid (nt "Vaata", "Muuda") annab vaade slotiga "actions".
const STATUS_BADGE_CLASSES = {
  U: 'text-bg-secondary',
  P: 'text-bg-success',
  D: 'text-bg-danger',
}

export default {
  name: 'TrainingSummaryCard',
  components: { FlagIcon },
  props: {
    training: Object,
  },
  computed: {
    statusBadgeClass() {
      return STATUS_BADGE_CLASSES[this.training.status]
    },

    lecturerNames() {
      const names = this.training.lecturers.map((lecturer) => lecturer.lecturerName)
      return names.length > 0 ? names.join(', ') : '—'
    },

    fundingTypeNames() {
      const names = this.training.fundingTypes.map((fundingType) => fundingType.fundingTypeName)
      return names.length > 0 ? names.join(', ') : '—'
    },

    settings() {
      const settings = []
      if (this.training.isOrderable) {
        settings.push(this.$t('trainingSummaryCard.orderable'))
      }
      if (this.training.isPromoted) {
        settings.push(this.$t('trainingSummaryCard.promoted'))
      }
      return settings.length > 0 ? settings.join(', ') : '—'
    },
  },
}
</script>

<template>
  <div class="mb-4 rounded-2xl border border-line bg-white p-5 sm:p-6">
    <div>
      <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
        <div class="flex flex-wrap items-center gap-2">
          <h2 class="text-xl font-bold">{{ training.title }}</h2>
          <span class="badge" :class="statusBadgeClass">
            {{ $t(`adminTrainings.status.${training.status}`) }}
          </span>
        </div>
        <div class="flex gap-2">
          <slot name="actions"></slot>
        </div>
      </div>
      <dl
        class="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-[15px] lg:grid-cols-[auto_1fr_auto_1fr]"
      >
        <dt class="text-muted">{{ $t('trainingSummaryCard.category') }}</dt>
        <dd class="font-semibold">{{ training.categoryName }}</dd>
        <dt class="text-muted">{{ $t('trainingSummaryCard.trainingLanguage') }}</dt>
        <dd class="font-semibold">
          <FlagIcon :flag-icon-code="training.trainingLanguageFlagIconCode" class="mr-1" />
          {{ training.trainingLanguageCode }}
        </dd>
        <dt class="text-muted">{{ $t('trainingSummaryCard.location') }}</dt>
        <dd class="font-semibold">{{ training.locationName }}</dd>
        <dt class="text-muted">{{ $t('trainingSummaryCard.lecturers') }}</dt>
        <dd class="font-semibold">{{ lecturerNames }}</dd>
        <dt class="text-muted">{{ $t('trainingSummaryCard.funding') }}</dt>
        <dd class="font-semibold">{{ fundingTypeNames }}</dd>
        <dt class="text-muted">{{ $t('trainingSummaryCard.settings') }}</dt>
        <dd class="font-semibold">{{ settings }}</dd>
      </dl>
    </div>
  </div>
</template>
