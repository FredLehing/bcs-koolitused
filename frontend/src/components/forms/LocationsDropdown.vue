<script>
export default {
  name: 'LocationsDropdown',
  props: {
    locationId: {
      type: Number,
      default: 0,
    },
    locations: Array,
    firstOptionLabel: {
      type: String,
      default: '',
    },
    isDisabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-new-location-selected'],
}
</script>

<template>
  <select
    :value="locationId"
    @change="$emit('event-new-location-selected', Number($event.target.value))"
    :disabled="isDisabled"
    class="form-select"
  >
    <option :value="0">{{ firstOptionLabel }}</option>
    <option v-for="location in locations" :key="location.locationId" :value="location.locationId">
      {{ location.locationName }}
    </option>
  </select>
</template>
