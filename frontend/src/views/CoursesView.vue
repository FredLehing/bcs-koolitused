<script>
import { mapState } from 'pinia'
import { PhFunnel, PhMagnifyingGlass, PhX } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import CourseService from '@/api-services/CourseService.js'
import CategoryService from '@/api-services/CategoryService.js'
import FundingTypeService from '@/api-services/FundingTypeService.js'
import LanguageService from '@/api-services/LanguageService.js'
import NavigationService from '@/services/NavigationService.js'
import PaginationNav from '@/components/common/PaginationNav.vue'
import CourseCard from '@/components/course/CourseCard.vue'
import CourseFilters from '@/components/forms/CourseFilters.vue'
import HelpTip from '@/components/common/HelpTip.vue'

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
  components: {
    HelpTip,
    PhFunnel,
    PhMagnifyingGlass,
    PhX,
    PaginationNav,
    CourseCard,
    CourseFilters,
  },
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
      // Kitsal ekraanil on filtrid peidetud paneelis
      isFilterPanelOpen: false,
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    hasActiveFilters() {
      return this.activeFilterCount > 0
    },

    activeFilterCount() {
      const defaultFilters = createDefaultFilters()
      return Object.keys(defaultFilters).filter(
        (filterName) => this.filters[filterName] !== defaultFilters[filterName],
      ).length
    },
  },
  watch: {
    // Keele vahetus → kaardid ja valikud uues keeles (filtrid ja leht jäävad)
    contentLang() {
      this.getCategories()
      this.getFundingTypes()
      this.getCourses()
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
  },
  beforeMount() {
    this.getLanguages()
    this.getCategories()
    this.getFundingTypes()
    this.getCourses()
  },
}
</script>

<template>
  <div class="mx-auto flex w-full max-w-6xl flex-1 flex-col px-4 py-8 sm:px-6 sm:py-10">
    <h1 class="mb-6 text-3xl font-extrabold tracking-tight sm:text-4xl">
      {{ $t('courses.title') }}
    </h1>

    <div class="flex flex-1 flex-col gap-6 lg:flex-row lg:items-start lg:gap-8">
      <!-- Filtrid: kitsal ekraanil nupp + avatav paneel, laial ekraanil külgriba -->
      <aside class="lg:sticky lg:top-24 lg:w-72 lg:shrink-0">
        <button
          @click="isFilterPanelOpen = !isFilterPanelOpen"
          :aria-expanded="isFilterPanelOpen"
          aria-controls="course-filter-panel"
          class="btn btn-outline-secondary w-full lg:hidden"
          type="button"
        >
          <PhFunnel :size="18" />
          {{ $t('courses.filters.title') }}
          <span v-if="activeFilterCount > 0" class="badge text-bg-primary">
            {{ activeFilterCount }}
          </span>
        </button>
        <div
          id="course-filter-panel"
          :class="isFilterPanelOpen ? 'block' : 'hidden'"
          class="mt-3 rounded-2xl border border-line bg-white p-5 lg:mt-0 lg:block"
        >
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
      </aside>

      <div class="flex min-w-0 flex-1 flex-col gap-4">
        <form class="flex items-center gap-2" @submit.prevent="handleSearchClick">
          <div
            class="flex min-h-12 flex-1 items-center gap-2 rounded-xl border border-brand-200 bg-white px-3 focus-within:border-brand-600 focus-within:ring-3 focus-within:ring-brand-600/15"
          >
            <PhMagnifyingGlass :size="20" class="shrink-0 text-muted" />
            <input
              ref="searchInput"
              v-model="searchText"
              type="text"
              class="min-w-0 flex-1 bg-transparent outline-none"
              :placeholder="$t('courses.searchPlaceholder')"
              :aria-label="$t('courses.searchPlaceholder')"
              @keyup.esc="handleClearSearch"
            />
            <button
              v-if="searchText"
              type="button"
              class="btn btn-link btn-sm text-muted"
              :title="$t('trainings.clearSearch')"
              :aria-label="$t('trainings.clearSearch')"
              @click="handleClearSearch"
            >
              <PhX :size="16" />
            </button>
          </div>
          <button type="submit" class="btn btn-primary min-h-12">
            {{ $t('trainings.search') }}
          </button>
          <HelpTip :text="$t('trainings.searchHelp')" />
        </form>
        <p v-if="appliedSearchText" class="text-muted">
          {{ $t('trainings.searchResults', { query: appliedSearchText }) }}
          <strong class="text-ink">{{ $t('courses.resultCount', totalElements) }}</strong>
          ·
          <button type="button" class="btn btn-link" @click="handleClearSearch">
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
          class="rounded-2xl border border-dashed border-brand-200 bg-white px-4 py-10 text-center text-muted"
        >
          <template v-if="appliedSearchText">
            <p class="mb-1 font-semibold text-ink">
              {{ $t('courses.noSearchResults', { query: appliedSearchText }) }}
            </p>
            <p class="mb-4">{{ $t('trainings.noSearchResultsHint') }}</p>
            <button type="button" class="btn btn-outline-primary" @click="handleClearSearch">
              {{ $t('courses.showAllCourses') }}
            </button>
          </template>
          <p v-else>{{ $t('courses.noResults') }}</p>
        </div>
        <PaginationNav
          :page="page"
          :total-pages="totalPages"
          @event-page-changed="handlePageChanged"
          class="mt-auto pt-4"
        />
      </div>
    </div>
  </div>
</template>
