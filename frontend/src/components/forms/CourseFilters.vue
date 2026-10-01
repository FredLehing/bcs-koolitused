<script>
import CategoriesDropdown from '@/components/forms/CategoriesDropdown.vue'
import DateInput from '@/components/forms/DateInput.vue'
import LanguagesDropdown from '@/components/forms/LanguagesDropdown.vue'

// Avaliku koolituste kalendri filtrid (CoursesView, vasak veerg). Iga muutus rakendub kohe.
// ID 0 = kõik, '' / null = filtrit ei rakendata.
export default {
  name: 'CourseFilters',
  components: { CategoriesDropdown, DateInput, LanguagesDropdown },
  props: {
    filters: Object,
    categories: Array,
    languages: Array,
    fundingTypes: Array,
    hasActiveFilters: Boolean,
  },
  emits: ['event-filter-changed', 'event-clear-clicked'],
  computed: {
    attendanceOptions() {
      return [
        { value: null, label: this.$t('courses.filters.all') },
        { value: 'ONSITE', label: this.$t('courses.onSite') },
        { value: 'ONLINE', label: this.$t('courses.online') },
      ]
    },

    fundingTypeOptions() {
      return [
        { fundingTypeId: 0, fundingTypeName: this.$t('courses.filters.all') },
        ...this.fundingTypes,
      ]
    },
  },
}
</script>

<template>
  <div class="d-flex flex-column gap-3 mb-4">
    <div>
      <label class="form-label fw-semibold" for="course-filter-start-date-from">
        {{ $t('courses.filters.startDateFrom') }}
      </label>
      <DateInput
        :date="filters.startDateFrom"
        input-id="course-filter-start-date-from"
        @event-new-date-input="$emit('event-filter-changed', 'startDateFrom', $event)"
      />
    </div>
    <div>
      <label class="form-label fw-semibold" for="course-filter-start-date-to">
        {{ $t('courses.filters.startDateTo') }}
      </label>
      <DateInput
        :date="filters.startDateTo"
        input-id="course-filter-start-date-to"
        @event-new-date-input="$emit('event-filter-changed', 'startDateTo', $event)"
      />
    </div>

    <fieldset>
      <legend class="form-label fw-semibold fs-6">{{ $t('courses.filters.attendance') }}</legend>
      <div
        v-for="attendanceOption in attendanceOptions"
        :key="attendanceOption.label"
        class="form-check"
      >
        <input
          :id="`course-filter-attendance-${attendanceOption.value ?? 'all'}`"
          :checked="filters.attendance === attendanceOption.value"
          @change="$emit('event-filter-changed', 'attendance', attendanceOption.value)"
          class="form-check-input"
          type="radio"
          name="course-filter-attendance"
        />
        <label
          class="form-check-label"
          :for="`course-filter-attendance-${attendanceOption.value ?? 'all'}`"
        >
          {{ attendanceOption.label }}
        </label>
      </div>
    </fieldset>

    <div class="form-check form-switch">
      <input
        id="course-filter-hide-full"
        :checked="filters.hideFull"
        @change="$emit('event-filter-changed', 'hideFull', $event.target.checked)"
        class="form-check-input"
        type="checkbox"
        role="switch"
      />
      <label class="form-check-label" for="course-filter-hide-full">
        {{ $t('courses.filters.hideFull') }}
      </label>
    </div>

    <div>
      <label class="form-label fw-semibold">{{ $t('courses.filters.trainingLanguage') }}</label>
      <LanguagesDropdown
        :language-id="filters.trainingLanguageId"
        :languages="languages"
        :first-option-label="$t('courses.filters.all')"
        @event-new-language-selected="$emit('event-filter-changed', 'trainingLanguageId', $event)"
      />
    </div>
    <div>
      <label class="form-label fw-semibold">{{ $t('courses.filters.category') }}</label>
      <CategoriesDropdown
        :category-id="filters.categoryId"
        :categories="categories"
        :first-option-label="$t('courses.filters.all')"
        @event-new-category-selected="$emit('event-filter-changed', 'categoryId', $event)"
      />
    </div>

    <fieldset>
      <legend class="form-label fw-semibold fs-6">{{ $t('courses.filters.fundingType') }}</legend>
      <div
        v-for="fundingType in fundingTypeOptions"
        :key="fundingType.fundingTypeId"
        class="form-check"
      >
        <input
          :id="`course-filter-funding-type-${fundingType.fundingTypeId}`"
          :checked="filters.fundingTypeId === fundingType.fundingTypeId"
          @change="$emit('event-filter-changed', 'fundingTypeId', fundingType.fundingTypeId)"
          class="form-check-input"
          type="radio"
          name="course-filter-funding-type"
        />
        <label
          class="form-check-label"
          :for="`course-filter-funding-type-${fundingType.fundingTypeId}`"
        >
          {{ fundingType.fundingTypeName }}
        </label>
      </div>
    </fieldset>

    <button
      v-if="hasActiveFilters"
      @click="$emit('event-clear-clicked')"
      class="btn btn-link p-0 align-self-start"
      type="button"
    >
      {{ $t('courses.filters.clear') }}
    </button>
  </div>
</template>
