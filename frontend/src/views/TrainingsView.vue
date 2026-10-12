<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import NavigationService from '@/services/NavigationService.js'
import TrainingCard from '@/components/TrainingCard.vue'
import PaginationNav from '@/components/common/PaginationNav.vue'
import { PhFunnel, PhMagnifyingGlass, PhX } from '@phosphor-icons/vue'
import LanguageService from '@/api-services/LanguageService.js'
import CategoryService from '@/api-services/CategoryService.js'
import FundingTypeService from '@/api-services/FundingTypeService.js'
import LanguagesDropdown from '@/components/forms/LanguagesDropdown.vue'
import CategoriesDropdown from '@/components/forms/CategoriesDropdown.vue'
import FundingTypesRadio from '@/components/forms/FundingTypesRadio.vue'
import HelpTip from '@/components/common/HelpTip.vue'
import TrainingsTabs from '@/components/common/TrainingsTabs.vue'

export default {
  name: 'TrainingsView',
  components: {
    FundingTypesRadio,
    TrainingCard,
    PaginationNav,
    HelpTip,
    TrainingsTabs,
    PhFunnel,
    PhMagnifyingGlass,
    PhX,
    LanguagesDropdown,
    CategoriesDropdown,
  },
  data() {
    return {
      categoryId: 0,
      fundingTypeId: 0,
      limit: 5,
      trainingsRequestId: 0,
      isLoadingTrainings: true,
      page: 0,
      trainingLanguageId: 0,
      // searchText = väljale sisestatud tekst, appliedSearchText = tekst, millega päring tehti
      searchText: '',
      appliedSearchText: '',
      totalPages: 0,
      totalElements: 0,
      trainings: [],
      categories: [],
      languages: [],
      fundingTypes: [],
      // Kitsal ekraanil on filtrid peidetud paneelis
      isFilterPanelOpen: false,
    }
  },
  computed: {
    // Kasutajaliidese keel (navbaris valitud) — sellega küsitakse koolituste tõlgitud väljad
    ...mapState(useLanguageStore, ['contentLang']),
    hasActiveFilters() {
      return this.activeFilterCount > 0
    },
    activeFilterCount() {
      return [this.trainingLanguageId, this.categoryId, this.fundingTypeId].filter((id) => id !== 0)
        .length
    },
  },
  watch: {
    // Keele vahetus navbaris → laadi koolitused uues keeles (filtrid ja lehekülg jäävad alles)
    contentLang() {
      this.getTrainings()
      this.getCategories()
      this.getFundingTypes()
    },
    // Väli tühjendati (käsitsi, × nupu või Esc-iga) → näita kohe kõiki koolitusi
    searchText(newSearchText) {
      if (newSearchText === '' && this.appliedSearchText !== '') {
        this.handleSearchClick()
      }
    },
  },
  methods: {
    handleClearFiltersClick() {
      if (!this.hasActiveFilters) return
      this.trainingLanguageId = 0
      this.categoryId = 0
      this.fundingTypeId = 0
      this.page = 0
      this.getTrainings()
    },
    getTrainings() {
      const trainingsRequestId = ++this.trainingsRequestId
      this.isLoadingTrainings = true
      return TrainingService.sendGetTrainingsRequest(
        this.categoryId,
        this.fundingTypeId,
        this.limit,
        this.page,
        this.trainingLanguageId,
        this.contentLang,
        this.appliedSearchText,
      )
        .then((response) => {
          if (trainingsRequestId === this.trainingsRequestId) {
            return this.handleGetTrainingsResponse(response)
          }
        })
        .catch(() => {
          if (trainingsRequestId === this.trainingsRequestId) {
            NavigationService.navigateToErrorView()
          }
        })
        .finally(() => {
          if (trainingsRequestId === this.trainingsRequestId) {
            this.isLoadingTrainings = false
          }
        })
    },
    handleGetTrainingsResponse(response) {
      // Keele vahetusel võib tulemuste arv väheneda; jätka viimasel olemasoleval lehel.
      const lastPage = Math.max(0, response.data.totalPages - 1)
      if (this.page > lastPage) {
        this.page = lastPage
        return this.getTrainings()
      }
      this.totalPages = response.data.totalPages
      this.totalElements = response.data.totalElements
      this.trainings = response.data.trainingSummaries
    },
    handleSearchClick() {
      this.appliedSearchText = this.searchText.trim()
      this.page = 0
      this.getTrainings()
    },
    handleClearSearch() {
      this.searchText = ''
      this.$refs.searchInput.focus()
    },
    handlePageChanged(newPage) {
      this.page = newPage
      this.getTrainings()
    },
    getLanguages() {
      LanguageService.sendGetLanguagesRequest()
        .then((response) => (this.languages = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },
    getCategories() {
      const contentLang = this.contentLang
      return CategoryService.sendGetCategoriesRequest(contentLang)
        .then((response) => {
          if (contentLang === this.contentLang) this.categories = response.data
        })
        .catch(() => {
          if (contentLang === this.contentLang) NavigationService.navigateToErrorView()
        })
    },
    getFundingTypes() {
      const contentLang = this.contentLang
      return FundingTypeService.sendGetFundingTypesRequest(contentLang)
        .then((response) => {
          if (contentLang === this.contentLang) this.fundingTypes = response.data
        })
        .catch(() => {
          if (contentLang === this.contentLang) NavigationService.navigateToErrorView()
        })
    },
    handleNewLanguageSelected(newLanguageId) {
      if (this.trainingLanguageId === newLanguageId) return
      this.trainingLanguageId = newLanguageId
      this.page = 0
      this.getTrainings()
    },
    handleNewCategorySelected(newCategoryId) {
      if (this.categoryId === newCategoryId) return
      this.categoryId = newCategoryId
      this.page = 0
      this.getTrainings()
    },

    handleNewFundingTypeSelected(newFundingTypeId) {
      if (this.fundingTypeId === newFundingTypeId) return
      this.fundingTypeId = newFundingTypeId
      this.page = 0
      this.getTrainings()
    },
  },
  beforeMount() {
    const appliedSearchText = this.$route.query.searchText ?? ''
    this.appliedSearchText = appliedSearchText
    this.searchText = appliedSearchText
    this.getTrainings()
    this.getLanguages()
    this.getCategories()
    this.getFundingTypes()
  },
  beforeUnmount() {
    this.trainingsRequestId++
  },
}
</script>

<template>
  <div class="mx-auto flex w-full max-w-6xl flex-1 flex-col px-4 py-8 sm:px-6 sm:py-10">
    <TrainingsTabs />
    <h1 class="mb-6 text-3xl font-extrabold tracking-tight sm:text-4xl">
      {{ $t('navbar.ourTrainings') }}
    </h1>

    <div class="flex flex-1 flex-col gap-6 lg:flex-row lg:items-start lg:gap-8">
      <!-- Filtrid: kitsal ekraanil nupp + avatav paneel, laial ekraanil külgriba -->
      <aside
        class="lg:sticky lg:top-24 lg:w-72 lg:shrink-0"
        :aria-label="$t('trainings.filters.title')"
      >
        <button
          @click="isFilterPanelOpen = !isFilterPanelOpen"
          :aria-expanded="isFilterPanelOpen"
          aria-controls="training-filter-panel"
          class="btn btn-outline-secondary w-full lg:hidden"
          type="button"
        >
          <PhFunnel :size="18" />
          {{ $t('trainings.filters.title') }}
          <span v-if="activeFilterCount > 0" class="badge text-bg-primary">
            {{ activeFilterCount }}
          </span>
        </button>
        <div
          id="training-filter-panel"
          :class="isFilterPanelOpen ? 'block' : 'hidden'"
          class="mt-3 rounded-2xl border border-line bg-white p-5 lg:mt-0 lg:block"
        >
          <div class="flex flex-col gap-5">
            <div>
              <label class="form-label" for="training-filter-language">
                {{ $t('trainings.filters.trainingLanguage') }}
              </label>
              <LanguagesDropdown
                id="training-filter-language"
                :languages="languages"
                :language-id="trainingLanguageId"
                @event-new-language-selected="handleNewLanguageSelected"
                :first-option-label="$t('trainings.filters.allLanguages')"
              />
            </div>
            <div>
              <label class="form-label" for="training-filter-category">
                {{ $t('trainings.filters.category') }}
              </label>
              <CategoriesDropdown
                id="training-filter-category"
                :categories="categories"
                :category-id="categoryId"
                @event-new-category-selected="handleNewCategorySelected"
                :first-option-label="$t('trainings.filters.allCategories')"
              />
            </div>
            <fieldset>
              <legend class="form-label">
                {{ $t('trainings.filters.fundingType') }}
              </legend>
              <FundingTypesRadio
                :funding-types="fundingTypes"
                :funding-type-id="fundingTypeId"
                @event-new-fundingtype-selected="handleNewFundingTypeSelected"
                :first-option-label="$t('trainings.filters.all')"
              />
            </fieldset>
            <button
              v-if="hasActiveFilters"
              @click="handleClearFiltersClick"
              class="btn btn-link self-start"
              type="button"
            >
              {{ $t('trainings.filters.clear') }}
            </button>
          </div>
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
              :placeholder="$t('trainings.searchPlaceholder')"
              :aria-label="$t('trainings.searchPlaceholder')"
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
          <strong class="text-ink">{{ $t('trainings.resultCount', totalElements) }}</strong>
          ·
          <button type="button" class="btn btn-link" @click="handleClearSearch">
            {{ $t('trainings.cancelSearch') }}
          </button>
        </p>

        <TrainingCard
          v-for="training in trainings"
          :key="training.trainingId"
          :training="training"
        />
        <div
          v-if="!isLoadingTrainings && trainings.length === 0"
          class="rounded-2xl border border-dashed border-brand-200 bg-white px-4 py-10 text-center text-muted"
        >
          <template v-if="appliedSearchText">
            <p class="mb-1 font-semibold text-ink">
              {{ $t('trainings.noSearchResults', { query: appliedSearchText }) }}
            </p>
            <p class="mb-4">{{ $t('trainings.noSearchResultsHint') }}</p>
            <button type="button" class="btn btn-outline-primary" @click="handleClearSearch">
              {{ $t('trainings.showAllTrainings') }}
            </button>
          </template>
          <p v-else>{{ $t('trainings.noResults') }}</p>
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
