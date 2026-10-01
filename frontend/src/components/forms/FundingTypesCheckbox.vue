<script>
export default {
  name: 'FundingTypesCheckbox',
  props: {
    fundingTypes: Array,
    selectedFundingTypeIds: Array,
    isDisabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-funding-type-checkbox-updated'],
  methods: {
    emitEventFundingTypeCheckboxUpdated(fundingTypeId, checked) {
      let updatedCheckbox = {
        fundingTypeId: fundingTypeId,
        checked: checked,
      }
      this.$emit('event-funding-type-checkbox-updated', updatedCheckbox)
    },
  },
}
</script>

<template>
  <div class="flex flex-wrap gap-x-6 gap-y-1">
    <div v-for="fundingType in fundingTypes" :key="fundingType.fundingTypeId" class="form-check">
      <input
        @change="
          emitEventFundingTypeCheckboxUpdated(fundingType.fundingTypeId, $event.target.checked)
        "
        :checked="selectedFundingTypeIds.includes(fundingType.fundingTypeId)"
        :disabled="isDisabled"
        :id="'fundingType' + fundingType.fundingTypeId"
        class="form-check-input"
        type="checkbox"
      />
      <label class="form-check-label" :for="'fundingType' + fundingType.fundingTypeId">
        {{ fundingType.fundingTypeName }}
      </label>
    </div>
  </div>
</template>
