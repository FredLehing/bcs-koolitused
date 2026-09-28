<script>
import CategoriesDropdown from '@/components/forms/CategoriesDropdown.vue'
import LanguagesDropdown from '@/components/forms/LanguagesDropdown.vue'
import LocationsDropdown from '@/components/forms/LocationsDropdown.vue'
import FundingTypesCheckbox from '@/components/forms/FundingTypesCheckbox.vue'

export default {
  name: 'TrainingDataForm',
  components: { FundingTypesCheckbox, LocationsDropdown, LanguagesDropdown, CategoriesDropdown },
  props: {
    training: Object,
    categories: Array,
    languages: Array,
    locations: Array,
    fundingTypes: Array,
    isDisabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: [
    'event-new-category-selected',
    'event-new-training-language-selected',
    'event-new-location-selected',
    'event-select-lecturer-clicked',
    'event-funding-type-checkbox-updated',
    'event-is-orderable-changed',
    'event-is-promoted-changed',
  ],
}
</script>

<template>
  <fieldset class="border rounded p-3 mb-4">
    <legend class="float-none w-auto px-2 fs-5">Koolituse andmed</legend>
    <div class="row g-3 text-start">
      <div class="col-md-4">
        <label class="form-label">Kategooria</label>
        <CategoriesDropdown
          :category-id="training.categoryId"
          :categories="categories"
          :is-disabled="isDisabled"
          @event-new-category-selected="$emit('event-new-category-selected', $event)"
        />
      </div>
      <div class="col-md-4">
        <label class="form-label">Koolituse keel</label>
        <LanguagesDropdown
          :language-id="training.trainingLanguageId"
          :languages="languages"
          :is-disabled="isDisabled"
          @event-new-language-selected="$emit('event-new-training-language-selected', $event)"
        />
      </div>
      <div class="col-md-4">
        <label class="form-label">Toimumiskoht</label>
        <LocationsDropdown
          :location-id="training.locationId"
          :locations="locations"
          :is-disabled="isDisabled"
          @event-new-location-selected="$emit('event-new-location-selected', $event)"
        />
      </div>
      <div class="col-12">
        <label class="form-label fw-bold">Rahastus</label>
        <FundingTypesCheckbox
          :funding-types="fundingTypes"
          :selected-funding-type-ids="training.fundingTypeIds"
          :is-disabled="isDisabled"
          @event-funding-type-checkbox-updated="
            $emit('event-funding-type-checkbox-updated', $event)
          "
        />
      </div>
      <div class="col-12 d-flex gap-4">
        <div class="form-check form-switch">
          <input
            @change="$emit('event-is-orderable-changed', $event.target.checked)"
            :checked="training.isOrderable"
            :disabled="isDisabled"
            id="isOrderable"
            class="form-check-input"
            type="checkbox"
            role="switch"
          />
          <label class="form-check-label" for="isOrderable">Tellitav</label>
        </div>
        <div class="form-check form-switch">
          <input
            @change="$emit('event-is-promoted-changed', $event.target.checked)"
            :checked="training.isPromoted"
            :disabled="isDisabled"
            id="isPromoted"
            class="form-check-input"
            type="checkbox"
            role="switch"
          />
          <label class="form-check-label" for="isPromoted">Esile tõstetud</label>
        </div>
      </div>
      <div class="col-md-6">
        <label class="form-label fw-bold">Vaikimisi lektor</label>
        <div class="input-group">
          <input
            :value="training.defaultLecturerName ?? '— lektor puudub —'"
            class="form-control"
            type="text"
            readonly
          />
          <button
            @click="$emit('event-select-lecturer-clicked')"
            :disabled="isDisabled"
            class="btn btn-outline-secondary"
            type="button"
          >
            Vali lektor
          </button>
        </div>
      </div>
    </div>
  </fieldset>
</template>
