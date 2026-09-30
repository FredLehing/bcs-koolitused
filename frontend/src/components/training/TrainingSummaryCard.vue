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
  <div class="card mb-3">
    <div class="card-body">
      <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
        <div class="d-flex flex-wrap align-items-center gap-2">
          <h2 class="h4 mb-0">{{ training.title }}</h2>
          <span class="badge" :class="statusBadgeClass">
            {{ $t(`adminTrainings.status.${training.status}`) }}
          </span>
        </div>
        <div class="d-flex gap-2">
          <slot name="actions"></slot>
        </div>
      </div>
      <dl class="row mb-0 small">
        <dt class="col-sm-4 col-lg-2">{{ $t('trainingSummaryCard.category') }}</dt>
        <dd class="col-sm-8 col-lg-4">{{ training.categoryName }}</dd>
        <dt class="col-sm-4 col-lg-2">{{ $t('trainingSummaryCard.trainingLanguage') }}</dt>
        <dd class="col-sm-8 col-lg-4">
          <FlagIcon :flag-icon-code="training.trainingLanguageFlagIconCode" />
          {{ training.trainingLanguageCode }}
        </dd>
        <dt class="col-sm-4 col-lg-2">{{ $t('trainingSummaryCard.location') }}</dt>
        <dd class="col-sm-8 col-lg-4">{{ training.locationName }}</dd>
        <dt class="col-sm-4 col-lg-2">{{ $t('trainingSummaryCard.lecturers') }}</dt>
        <dd class="col-sm-8 col-lg-4">{{ lecturerNames }}</dd>
        <dt class="col-sm-4 col-lg-2">{{ $t('trainingSummaryCard.funding') }}</dt>
        <dd class="col-sm-8 col-lg-4">{{ fundingTypeNames }}</dd>
        <dt class="col-sm-4 col-lg-2">{{ $t('trainingSummaryCard.settings') }}</dt>
        <dd class="col-sm-8 col-lg-4 mb-0">{{ settings }}</dd>
      </dl>
    </div>
  </div>
</template>
