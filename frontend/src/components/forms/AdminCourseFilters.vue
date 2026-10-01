<script>
import CategoriesDropdown from '@/components/forms/CategoriesDropdown.vue'
import DateInput from '@/components/forms/DateInput.vue'
import LanguagesDropdown from '@/components/forms/LanguagesDropdown.vue'

// Kaart "Otsingu filtrid" (AdminAllCoursesView). Muudab ainult sisestatavaid filtreid (filters);
// päring tehakse alles "Filtreeri" nupuga. Valikulised filtrid: null / '' = "Kõik".
export default {
  name: 'AdminCourseFilters',
  components: { CategoriesDropdown, DateInput, LanguagesDropdown },
  props: {
    filters: Object,
    categories: Array,
    languages: Array,
  },
  emits: ['event-filter-changed', 'event-filter-clicked', 'event-clear-clicked'],
  methods: {
    // Boolean filter <select>-is: null ↔ '', true ↔ 'true', false ↔ 'false'
    toSelectValue(booleanValue) {
      return booleanValue === null ? '' : String(booleanValue)
    },

    changeBooleanFilter(filterName, selectValue) {
      const booleanValue = selectValue === '' ? null : selectValue === 'true'
      this.$emit('event-filter-changed', filterName, booleanValue)
    },

    changeTextFilter(filterName, selectValue) {
      this.$emit('event-filter-changed', filterName, selectValue === '' ? null : selectValue)
    },
  },
}
</script>

<template>
  <div id="admin-course-filters" class="card mb-3">
    <div class="card-body">
      <h2 class="h5 card-title mb-3">{{ $t('adminAllCourses.filters.title') }}</h2>
      <div class="row g-3">
        <div class="col-md-6 col-lg-3">
          <label class="form-label" for="filter-start-date-from">
            {{ $t('adminAllCourses.filters.startDateFrom') }}
          </label>
          <DateInput
            :date="filters.startDateFrom"
            input-id="filter-start-date-from"
            @event-new-date-input="$emit('event-filter-changed', 'startDateFrom', $event)"
          />
        </div>
        <div class="col-md-6 col-lg-3">
          <label class="form-label" for="filter-start-date-to">
            {{ $t('adminAllCourses.filters.startDateTo') }}
          </label>
          <DateInput
            :date="filters.startDateTo"
            input-id="filter-start-date-to"
            @event-new-date-input="$emit('event-filter-changed', 'startDateTo', $event)"
          />
        </div>
        <div class="col-md-6 col-lg-3">
          <label class="form-label">{{ $t('adminAllCourses.filters.category') }}</label>
          <CategoriesDropdown
            :category-id="filters.categoryId"
            :categories="categories"
            :first-option-label="$t('adminAllCourses.filters.all')"
            @event-new-category-selected="$emit('event-filter-changed', 'categoryId', $event)"
          />
        </div>
        <div class="col-md-6 col-lg-3">
          <label class="form-label">{{ $t('adminAllCourses.filters.trainingLanguage') }}</label>
          <LanguagesDropdown
            :language-id="filters.trainingLanguageId"
            :languages="languages"
            :first-option-label="$t('adminAllCourses.filters.all')"
            @event-new-language-selected="
              $emit('event-filter-changed', 'trainingLanguageId', $event)
            "
          />
        </div>
        <div class="col-md-4">
          <label for="filter-course-status" class="form-label">
            {{ $t('adminAllCourses.filters.status') }}
          </label>
          <select
            id="filter-course-status"
            :value="filters.status ?? ''"
            @change="changeTextFilter('status', $event.target.value)"
            class="form-select"
          >
            <option value="">{{ $t('adminAllCourses.filters.all') }}</option>
            <option v-for="status in ['U', 'O', 'F', 'X']" :key="status" :value="status">
              {{ $t(`courseStatus.${status}`) }}
            </option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="filter-attendance" class="form-label">
            {{ $t('adminAllCourses.filters.attendance') }}
          </label>
          <select
            id="filter-attendance"
            :value="filters.attendance ?? ''"
            @change="changeTextFilter('attendance', $event.target.value)"
            class="form-select"
          >
            <option value="">{{ $t('adminAllCourses.filters.all') }}</option>
            <option value="ONSITE">{{ $t('adminAllCourses.filters.onSite') }}</option>
            <option value="ONLINE">{{ $t('adminAllCourses.filters.online') }}</option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="filter-course-promoted" class="form-label">
            {{ $t('adminAllCourses.filters.promoted') }}
          </label>
          <select
            id="filter-course-promoted"
            :value="toSelectValue(filters.isPromoted)"
            @change="changeBooleanFilter('isPromoted', $event.target.value)"
            class="form-select"
          >
            <option value="">{{ $t('adminAllCourses.filters.all') }}</option>
            <option value="true">{{ $t('adminAllCourses.filters.yes') }}</option>
            <option value="false">{{ $t('adminAllCourses.filters.no') }}</option>
          </select>
        </div>
      </div>
      <div class="d-flex flex-wrap gap-2 mt-3">
        <button @click="$emit('event-filter-clicked')" class="btn btn-primary" type="button">
          {{ $t('adminAllCourses.filters.filter') }}
        </button>
        <button
          @click="$emit('event-clear-clicked')"
          class="btn btn-outline-secondary"
          type="button"
        >
          {{ $t('adminAllCourses.filters.clear') }}
        </button>
      </div>
    </div>
  </div>
</template>
