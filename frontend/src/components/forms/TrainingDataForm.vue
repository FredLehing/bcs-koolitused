<script>
import CategoriesDropdown from '@/components/forms/CategoriesDropdown.vue'
import LanguagesDropdown from '@/components/forms/LanguagesDropdown.vue'
import LocationsDropdown from '@/components/forms/LocationsDropdown.vue'
import FundingTypesCheckbox from '@/components/forms/FundingTypesCheckbox.vue'
import LecturersPicker from '@/components/forms/LecturersPicker.vue'

export default {
  name: 'TrainingDataForm',
  components: {
    FundingTypesCheckbox,
    LocationsDropdown,
    LanguagesDropdown,
    CategoriesDropdown,
    LecturersPicker,
  },
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
    'event-lecturers-changed',
    'event-funding-type-checkbox-updated',
    'event-is-orderable-changed',
    'event-is-promoted-changed',
  ],
}
</script>

<template>
  <fieldset class="border rounded p-3 mb-4">
    <legend class="float-none w-auto px-2 fs-5">{{ $t('trainingForm.data.legend') }}</legend>
    <div class="row g-3 text-start">
      <div class="col-md-4">
        <label class="form-label">{{ $t('trainingForm.data.category') }}</label>
        <CategoriesDropdown
          :category-id="training.categoryId"
          :categories="categories"
          :first-option-label="$t('trainingForm.data.selectCategory')"
          :is-disabled="isDisabled"
          @event-new-category-selected="$emit('event-new-category-selected', $event)"
        />
      </div>
      <div class="col-md-4">
        <label class="form-label">{{ $t('trainingForm.data.trainingLanguage') }}</label>
        <LanguagesDropdown
          :language-id="training.trainingLanguageId"
          :languages="languages"
          :first-option-label="$t('trainingForm.data.selectLanguage')"
          :is-disabled="isDisabled"
          @event-new-language-selected="$emit('event-new-training-language-selected', $event)"
        />
      </div>
      <div class="col-md-4">
        <label class="form-label">{{ $t('trainingForm.data.location') }}</label>
        <LocationsDropdown
          :location-id="training.locationId"
          :locations="locations"
          :first-option-label="$t('trainingForm.data.selectLocation')"
          :is-disabled="isDisabled"
          @event-new-location-selected="$emit('event-new-location-selected', $event)"
        />
      </div>
      <div class="col-12">
        <label class="form-label fw-bold">{{ $t('trainingForm.data.funding') }}</label>
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
          <label class="form-check-label" for="isOrderable">{{
            $t('trainingForm.data.orderable')
          }}</label>
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
          <label class="form-check-label" for="isPromoted">{{
            $t('trainingForm.data.promoted')
          }}</label>
        </div>
      </div>
      <div class="col-md-6">
        <label class="form-label fw-bold">{{ $t('trainingForm.data.lecturers') }}</label>
        <LecturersPicker
          :lecturers="training.lecturers"
          :is-disabled="isDisabled"
          @event-lecturers-changed="$emit('event-lecturers-changed', $event)"
        />
      </div>
    </div>
  </fieldset>
</template>
