<script>
export default {
  name: 'FundingTypesRadio',
  props: {
    fundingTypeId: {
      type: Number,
      default: 0,
    },
    fundingTypes: Array,
    firstOptionLabel: {
      type: String,
      default: '',
    },
    isDisabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-new-fundingtype-selected'],
}
</script>

<template>
  <div class="flex flex-col">
    <div class="form-check">
      <input
        @change="$emit('event-new-fundingtype-selected', 0)"
        :checked="fundingTypeId === 0"
        :disabled="isDisabled"
        id="fundingType0"
        class="form-check-input"
        type="radio"
        name="fundingType"
      />
      <label class="form-check-label" for="fundingType0">
        {{ firstOptionLabel }}
      </label>
    </div>

    <div v-for="fundingType in fundingTypes" :key="fundingType.fundingTypeId" class="form-check">
      <input
        @change="$emit('event-new-fundingtype-selected', fundingType.fundingTypeId)"
        :checked="fundingTypeId === fundingType.fundingTypeId"
        :disabled="isDisabled"
        :id="'fundingType' + fundingType.fundingTypeId"
        class="form-check-input"
        type="radio"
        name="fundingType"
      />
      <label class="form-check-label" :for="'fundingType' + fundingType.fundingTypeId">
        {{ fundingType.fundingTypeName }}
      </label>
    </div>
  </div>
</template>
