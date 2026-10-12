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
  <section
    class="rounded-2xl border border-line bg-white p-6"
    aria-labelledby="training-data-heading"
  >
    <h2 id="training-data-heading" class="mb-4 text-lg font-bold">
      {{ $t('trainingForm.data.legend') }}
    </h2>
    <div class="grid gap-4 md:grid-cols-3">
      <div>
        <label class="form-label" for="training-category">
          {{ $t('trainingForm.data.category') }}
        </label>
        <!-- id läheb läbi komponendi <select>-ile -->
        <CategoriesDropdown
          id="training-category"
          :category-id="training.categoryId"
          :categories="categories"
          :first-option-label="$t('trainingForm.data.selectCategory')"
          :is-disabled="isDisabled"
          @event-new-category-selected="$emit('event-new-category-selected', $event)"
        />
      </div>
      <div>
        <label class="form-label" for="training-language">
          {{ $t('trainingForm.data.trainingLanguage') }}
        </label>
        <LanguagesDropdown
          id="training-language"
          :language-id="training.trainingLanguageId"
          :languages="languages"
          :first-option-label="$t('trainingForm.data.selectLanguage')"
          :is-disabled="isDisabled"
          @event-new-language-selected="$emit('event-new-training-language-selected', $event)"
        />
      </div>
      <div>
        <label class="form-label" for="training-location">
          {{ $t('trainingForm.data.location') }}
        </label>
        <LocationsDropdown
          id="training-location"
          :location-id="training.locationId"
          :locations="locations"
          :first-option-label="$t('trainingForm.data.selectLocation')"
          :is-disabled="isDisabled"
          @event-new-location-selected="$emit('event-new-location-selected', $event)"
        />
      </div>
      <fieldset class="md:col-span-3">
        <legend class="form-label">{{ $t('trainingForm.data.funding') }}</legend>
        <FundingTypesCheckbox
          :funding-types="fundingTypes"
          :selected-funding-type-ids="training.fundingTypeIds"
          :is-disabled="isDisabled"
          @event-funding-type-checkbox-updated="
            $emit('event-funding-type-checkbox-updated', $event)
          "
        />
      </fieldset>
      <div class="flex flex-wrap gap-x-8 gap-y-1 md:col-span-3">
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
      <div class="md:col-span-2">
        <h3 class="form-label">
          {{ $t('trainingForm.data.lecturers') }}
        </h3>
        <LecturersPicker
          :lecturers="training.lecturers"
          :is-disabled="isDisabled"
          @event-lecturers-changed="$emit('event-lecturers-changed', $event)"
        />
      </div>
    </div>
  </section>
</template>
