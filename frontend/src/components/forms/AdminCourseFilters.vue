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
  <div id="admin-course-filters" class="card mb-4">
    <div class="card-body">
      <h2 class="mb-4 text-lg font-bold">{{ $t('adminAllCourses.filters.title') }}</h2>
      <div class="grid grid-cols-4 gap-4">
        <div>
          <label class="form-label" for="filter-start-date-from">
            {{ $t('adminAllCourses.filters.startDateFrom') }}
          </label>
          <DateInput
            :date="filters.startDateFrom"
            input-id="filter-start-date-from"
            @event-new-date-input="$emit('event-filter-changed', 'startDateFrom', $event)"
          />
        </div>
        <div>
          <label class="form-label" for="filter-start-date-to">
            {{ $t('adminAllCourses.filters.startDateTo') }}
          </label>
          <DateInput
            :date="filters.startDateTo"
            input-id="filter-start-date-to"
            @event-new-date-input="$emit('event-filter-changed', 'startDateTo', $event)"
          />
        </div>
        <div>
          <label class="form-label" for="filter-category">
            {{ $t('adminAllCourses.filters.category') }}
          </label>
          <CategoriesDropdown
            id="filter-category"
            :category-id="filters.categoryId"
            :categories="categories"
            :first-option-label="$t('adminAllCourses.filters.all')"
            @event-new-category-selected="$emit('event-filter-changed', 'categoryId', $event)"
          />
        </div>
        <div>
          <label class="form-label" for="filter-training-language">
            {{ $t('adminAllCourses.filters.trainingLanguage') }}
          </label>
          <LanguagesDropdown
            id="filter-training-language"
            :language-id="filters.trainingLanguageId"
            :languages="languages"
            :first-option-label="$t('adminAllCourses.filters.all')"
            @event-new-language-selected="
              $emit('event-filter-changed', 'trainingLanguageId', $event)
            "
          />
        </div>
        <div>
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
        <div>
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
        <div>
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
      <div class="mt-5 flex flex-wrap gap-2">
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
