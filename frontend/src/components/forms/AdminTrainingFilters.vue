<script>
import CategoriesDropdown from '@/components/forms/CategoriesDropdown.vue'
import LanguagesDropdown from '@/components/forms/LanguagesDropdown.vue'

// Kaart "Otsingu filtrid" (AdminTrainingsView). Muudab ainult sisestatavaid filtreid (filters);
// päring tehakse alles "Filtreeri" nupuga. Valikulised filtrid: null = "Kõik" / "Aktiivsed".
export default {
  name: 'AdminTrainingFilters',
  components: { CategoriesDropdown, LanguagesDropdown },
  props: {
    filters: Object,
    categories: Array,
    languages: Array,
    fundingTypes: Array,
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

    changeStatusFilter(selectValue) {
      this.$emit('event-filter-changed', 'status', selectValue === '' ? null : selectValue)
    },
  },
}
</script>

<template>
  <div id="admin-training-filters" class="card mb-3">
    <div class="card-body">
      <h2 class="h5 card-title mb-3">{{ $t('adminTrainings.filters.title') }}</h2>
      <div class="row g-3">
        <div class="col-md-6 col-lg-4">
          <label class="form-label">{{ $t('adminTrainings.filters.category') }}</label>
          <CategoriesDropdown
            :category-id="filters.categoryId"
            :categories="categories"
            :first-option-label="$t('adminTrainings.filters.all')"
            @event-new-category-selected="$emit('event-filter-changed', 'categoryId', $event)"
          />
        </div>
        <div class="col-md-6 col-lg-4">
          <label class="form-label">{{ $t('adminTrainings.filters.trainingLanguage') }}</label>
          <LanguagesDropdown
            :language-id="filters.trainingLanguageId"
            :languages="languages"
            :first-option-label="$t('adminTrainings.filters.all')"
            @event-new-language-selected="
              $emit('event-filter-changed', 'trainingLanguageId', $event)
            "
          />
        </div>
        <div class="col-md-6 col-lg-4">
          <label for="filter-funding-type" class="form-label">
            {{ $t('adminTrainings.filters.fundingType') }}
          </label>
          <select
            id="filter-funding-type"
            :value="filters.fundingTypeId"
            @change="$emit('event-filter-changed', 'fundingTypeId', Number($event.target.value))"
            class="form-select"
          >
            <option :value="0">{{ $t('adminTrainings.filters.all') }}</option>
            <option
              v-for="fundingType in fundingTypes"
              :key="fundingType.fundingTypeId"
              :value="fundingType.fundingTypeId"
            >
              {{ fundingType.fundingTypeName }}
            </option>
          </select>
        </div>
        <div class="col-md-6 col-lg-3">
          <label for="filter-status" class="form-label">
            {{ $t('adminTrainings.filters.status') }}
          </label>
          <select
            id="filter-status"
            :value="filters.status ?? ''"
            @change="changeStatusFilter($event.target.value)"
            class="form-select"
          >
            <option value="">{{ $t('adminTrainings.filters.active') }}</option>
            <option value="U">{{ $t('adminTrainings.status.U') }}</option>
            <option value="P">{{ $t('adminTrainings.status.P') }}</option>
            <option value="D">{{ $t('adminTrainings.status.D') }}</option>
          </select>
        </div>
        <div class="col-md-4 col-lg-3">
          <label for="filter-orderable" class="form-label">
            {{ $t('adminTrainings.filters.orderable') }}
          </label>
          <select
            id="filter-orderable"
            :value="toSelectValue(filters.isOrderable)"
            @change="changeBooleanFilter('isOrderable', $event.target.value)"
            class="form-select"
          >
            <option value="">{{ $t('adminTrainings.filters.all') }}</option>
            <option value="true">{{ $t('adminTrainings.filters.yes') }}</option>
            <option value="false">{{ $t('adminTrainings.filters.no') }}</option>
          </select>
        </div>
        <div class="col-md-4 col-lg-3">
          <label for="filter-promoted" class="form-label">
            {{ $t('adminTrainings.filters.promoted') }}
          </label>
          <select
            id="filter-promoted"
            :value="toSelectValue(filters.isPromoted)"
            @change="changeBooleanFilter('isPromoted', $event.target.value)"
            class="form-select"
          >
            <option value="">{{ $t('adminTrainings.filters.all') }}</option>
            <option value="true">{{ $t('adminTrainings.filters.yes') }}</option>
            <option value="false">{{ $t('adminTrainings.filters.no') }}</option>
          </select>
        </div>
        <div class="col-md-4 col-lg-3">
          <label for="filter-translations" class="form-label">
            {{ $t('adminTrainings.filters.translations') }}
          </label>
          <select
            id="filter-translations"
            :value="toSelectValue(filters.hasAllTranslations)"
            @change="changeBooleanFilter('hasAllTranslations', $event.target.value)"
            class="form-select"
          >
            <option value="">{{ $t('adminTrainings.filters.all') }}</option>
            <option value="true">{{ $t('adminTrainings.filters.translationsComplete') }}</option>
            <option value="false">{{ $t('adminTrainings.filters.translationsMissing') }}</option>
          </select>
        </div>
      </div>
      <div class="d-flex flex-wrap gap-2 mt-3">
        <button @click="$emit('event-filter-clicked')" class="btn btn-primary" type="button">
          {{ $t('adminTrainings.filters.filter') }}
        </button>
        <button
          @click="$emit('event-clear-clicked')"
          class="btn btn-outline-secondary"
          type="button"
        >
          {{ $t('adminTrainings.filters.clear') }}
        </button>
      </div>
    </div>
  </div>
</template>
