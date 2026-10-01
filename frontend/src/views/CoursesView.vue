<script>
import { mapState } from 'pinia'
import { Tooltip } from 'bootstrap'
import { PhQuestion, PhX } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import CourseService from '@/api-services/CourseService.js'
import CategoryService from '@/api-services/CategoryService.js'
import FundingTypeService from '@/api-services/FundingTypeService.js'
import LanguageService from '@/api-services/LanguageService.js'
import NavigationService from '@/services/NavigationService.js'
import PaginationNav from '@/components/common/PaginationNav.vue'
import CourseCard from '@/components/course/CourseCard.vue'
import CourseFilters from '@/components/forms/CourseFilters.vue'

const LIMIT = 5

// Filtrite vaikimisi väärtused: ID 0 = kõik, '' / null / false = filtrit ei rakendata
function createDefaultFilters() {
  return {
    startDateFrom: '',
    startDateTo: '',
    attendance: null,
    hideFull: false,
    trainingLanguageId: 0,
    categoryId: 0,
    fundingTypeId: 0,
  }
}

// Avalik koolituste kalender: /courses
export default {
  name: 'CoursesView',
  components: { PhQuestion, PhX, PaginationNav, CourseCard, CourseFilters },
  data() {
    return {
      filters: createDefaultFilters(),
      // searchText = väljale sisestatud tekst, appliedSearchText = tekst, millega päring tehti
      searchText: '',
      appliedSearchText: '',
      page: 0,
      totalPages: 0,
      totalElements: 0,
      courseSummaries: [],
      categories: [],
      languages: [],
      fundingTypes: [],
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    hasActiveFilters() {
      const defaultFilters = createDefaultFilters()
      return Object.keys(defaultFilters).some(
        (filterName) => this.filters[filterName] !== defaultFilters[filterName],
      )
    },
  },
  watch: {
    // Keele vahetus → kaardid ja valikud uues keeles (filtrid ja leht jäävad)
    contentLang() {
      this.getCategories()
      this.getFundingTypes()
      this.getCourses()
      this.$nextTick(() => this.updateSearchHelpTooltip())
    },

    // Väli tühjendati (käsitsi, × nupu või Esc-iga) → näita kohe kõiki toimumiskordi
    searchText(newSearchText) {
      if (newSearchText === '' && this.appliedSearchText !== '') {
        this.handleSearchClick()
      }
    },
  },
  methods: {
    getCourses() {
      CourseService.sendGetCoursesRequest({
        contentLang: this.contentLang,
        searchText: this.appliedSearchText,
        categoryId: this.filters.categoryId,
        trainingLanguageId: this.filters.trainingLanguageId,
        fundingTypeId: this.filters.fundingTypeId,
        attendance: this.filters.attendance,
        hideFull: this.filters.hideFull,
        startDateFrom: this.filters.startDateFrom || null,
        startDateTo: this.filters.startDateTo || null,
        page: this.page,
        limit: LIMIT,
      })
        .then((response) => this.handleGetCoursesResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Kui leht jäi tühjaks (nt keele vahetusel), liigutakse eelmisele lehele
    handleGetCoursesResponse(courseSummaryPageDto) {
      if (courseSummaryPageDto.courseSummaries.length === 0 && this.page > 0) {
        this.page = this.page - 1
        this.getCourses()
        return
      }
      this.totalPages = courseSummaryPageDto.totalPages
      this.totalElements = courseSummaryPageDto.totalElements
      this.courseSummaries = courseSummaryPageDto.courseSummaries
    },

    getCategories() {
      CategoryService.sendGetCategoriesRequest(this.contentLang)
        .then((response) => (this.categories = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getLanguages() {
      LanguageService.sendGetLanguagesRequest()
        .then((response) => (this.languages = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getFundingTypes() {
      FundingTypeService.sendGetFundingTypesRequest(this.contentLang)
        .then((response) => (this.fundingTypes = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Poolik kuupäev ("" DateInputist) ei käivita päringut enne, kui see muutub
    handleFilterChanged(filterName, filterValue) {
      if (this.filters[filterName] === filterValue) {
        return
      }
      this.filters[filterName] = filterValue
      this.page = 0
      this.getCourses()
    },

    handleClearFiltersClick() {
      this.filters = createDefaultFilters()
      this.page = 0
      this.getCourses()
    },

    handleSearchClick() {
      this.appliedSearchText = this.searchText.trim()
      this.page = 0
      this.getCourses()
    },

    handleClearSearch() {
      this.searchText = ''
      this.$refs.searchInput.focus()
    },

    handlePageChanged(newPage) {
      this.page = newPage
      this.getCourses()
    },

    // Bootstrap tooltip loeb teksti ainult loomisel, keele vahetusel tuleb see uuendada
    updateSearchHelpTooltip() {
      this.searchHelpTooltip.setContent({ '.tooltip-inner': this.$t('trainings.searchHelp') })
    },
  },
  beforeMount() {
    this.getLanguages()
    this.getCategories()
    this.getFundingTypes()
    this.getCourses()
  },
  mounted() {
    this.searchHelpTooltip = new Tooltip(this.$refs.searchHelp)
  },
  beforeUnmount() {
    this.searchHelpTooltip.dispose()
  },
}
</script>

<template>
  <div class="container d-flex flex-grow-1 flex-column">
    <h1 class="h3 mb-3">{{ $t('courses.title') }}</h1>
    <div class="row flex-grow-1">
      <div class="col-md-4 col-lg-3">
        <CourseFilters
          :filters="filters"
          :categories="categories"
          :languages="languages"
          :funding-types="fundingTypes"
          :has-active-filters="hasActiveFilters"
          @event-filter-changed="handleFilterChanged"
          @event-clear-clicked="handleClearFiltersClick"
        />
      </div>
      <div class="col-md-8 col-lg-9 d-flex flex-column">
        <div class="d-flex align-items-center gap-2 mb-3">
          <div class="input-group">
            <input
              ref="searchInput"
              v-model="searchText"
              type="text"
              class="form-control"
              :placeholder="$t('courses.searchPlaceholder')"
              :aria-label="$t('courses.searchPlaceholder')"
              @keyup.enter="handleSearchClick"
              @keyup.esc="handleClearSearch"
            />
            <button
              v-if="searchText"
              type="button"
              class="btn btn-outline-secondary"
              :title="$t('trainings.clearSearch')"
              :aria-label="$t('trainings.clearSearch')"
              @click="handleClearSearch"
            >
              <PhX :size="16" />
            </button>
            <button type="button" class="btn btn-primary" @click="handleSearchClick">
              {{ $t('trainings.search') }}
            </button>
          </div>
          <span
            ref="searchHelp"
            class="text-secondary"
            role="img"
            tabindex="0"
            data-bs-toggle="tooltip"
            data-bs-placement="left"
            :data-bs-title="$t('trainings.searchHelp')"
            :aria-label="$t('trainings.searchHelp')"
          >
            <PhQuestion :size="22" />
          </span>
        </div>
        <p v-if="appliedSearchText" class="text-secondary mb-3">
          {{ $t('trainings.searchResults', { query: appliedSearchText }) }}
          <strong>{{ $t('courses.resultCount', totalElements) }}</strong>
          ·
          <button type="button" class="btn btn-link p-0 align-baseline" @click="handleClearSearch">
            {{ $t('trainings.cancelSearch') }}
          </button>
        </p>

        <CourseCard
          v-for="courseSummary in courseSummaries"
          :key="courseSummary.courseId"
          :course="courseSummary"
        />
        <div
          v-if="courseSummaries.length === 0"
          class="text-center text-secondary border rounded py-4 px-3 mb-3"
        >
          <template v-if="appliedSearchText">
            <p class="fw-semibold mb-1">
              {{ $t('courses.noSearchResults', { query: appliedSearchText }) }}
            </p>
            <p class="mb-3">{{ $t('trainings.noSearchResultsHint') }}</p>
            <button type="button" class="btn btn-outline-primary" @click="handleClearSearch">
              {{ $t('courses.showAllCourses') }}
            </button>
          </template>
          <p v-else class="mb-0">{{ $t('courses.noResults') }}</p>
        </div>
        <PaginationNav
          :page="page"
          :total-pages="totalPages"
          @event-page-changed="handlePageChanged"
          class="mb-3 mt-auto"
        />
      </div>
    </div>
  </div>
</template>
