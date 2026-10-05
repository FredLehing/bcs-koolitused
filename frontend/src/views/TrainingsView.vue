<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import NavigationService from '@/services/NavigationService.js'
import TrainingCard from '@/components/TrainingCard.vue'
import PaginationNav from '@/components/common/PaginationNav.vue'
import { PhQuestion, PhX } from '@phosphor-icons/vue'
import { Tooltip } from 'bootstrap'
import LanguageService from '@/api-services/LanguageService.js'
import CategoryService from '@/api-services/CategoryService.js'
import FundingTypeService from '@/api-services/FundingTypeService.js'
import LanguagesDropdown from '@/components/forms/LanguagesDropdown.vue'
import CategoriesDropdown from '@/components/forms/CategoriesDropdown.vue'
import FundingTypesRadio from '@/components/forms/FundingTypesRadio.vue'
import TrainingsTabs from '@/components/common/TrainingsTabs.vue'

export default {
  name: 'TrainingsView',
  components: {
    FundingTypesRadio,
    TrainingCard,
    PaginationNav,
    PhQuestion,
    PhX,
    LanguagesDropdown,
    CategoriesDropdown,
    TrainingsTabs,
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
    }
  },
  computed: {
    // Kasutajaliidese keel (navbaris valitud) — sellega küsitakse koolituste tõlgitud väljad
    ...mapState(useLanguageStore, ['contentLang']),
    hasActiveFilters() {
      return this.trainingLanguageId !== 0 || this.categoryId !== 0 || this.fundingTypeId !== 0
    },
  },
  watch: {
    // Keele vahetus navbaris → laadi koolitused uues keeles (filtrid ja lehekülg jäävad alles)
    contentLang() {
      this.getTrainings()
      this.$nextTick(() => this.updateSearchHelpTooltip())
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
    // Bootstrap tooltip loeb teksti ainult loomisel, keele vahetusel tuleb see uuendada
    updateSearchHelpTooltip() {
      this.searchHelpTooltip?.setContent({ '.tooltip-inner': this.$t('trainings.searchHelp') })
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
  mounted() {
    this.searchHelpTooltip = new Tooltip(this.$refs.searchHelp)
  },
  beforeUnmount() {
    this.trainingsRequestId++
    this.searchHelpTooltip.dispose()
  },
}
</script>

<template>
  <div class="container d-flex flex-grow-1 flex-column">
    <TrainingsTabs />

    <div class="row flex-grow-1">
      <div class="col-md-4 col-lg-3">
        <aside class="d-flex flex-column gap-3 mb-4" :aria-label="$t('trainings.filters.title')">
          <div>
            <label class="form-label fw-semibold" for="training-filter-language">
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
            <label class="form-label fw-semibold" for="training-filter-category">
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
            <legend class="form-label fw-semibold fs-6">
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
            class="btn btn-link p-0 align-self-start"
            type="button"
          >
            {{ $t('trainings.filters.clear') }}
          </button>
        </aside>
      </div>
      <div class="col-md-8 col-lg-9 d-flex flex-column">
        <div class="d-flex align-items-center gap-2 mb-3">
          <div class="input-group">
            <input
              ref="searchInput"
              v-model="searchText"
              type="text"
              class="form-control"
              :placeholder="$t('trainings.searchPlaceholder')"
              :aria-label="$t('trainings.searchPlaceholder')"
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
          <strong>{{ $t('trainings.resultCount', totalElements) }}</strong>
          ·
          <button type="button" class="btn btn-link p-0 align-baseline" @click="handleClearSearch">
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
          class="text-center text-secondary border rounded py-4 px-3 mb-3"
        >
          <template v-if="appliedSearchText">
            <p class="fw-semibold mb-1">
              {{ $t('trainings.noSearchResults', { query: appliedSearchText }) }}
            </p>
            <p class="mb-3">{{ $t('trainings.noSearchResultsHint') }}</p>
            <button type="button" class="btn btn-outline-primary" @click="handleClearSearch">
              {{ $t('trainings.showAllTrainings') }}
            </button>
          </template>
          <p v-else class="mb-0">{{ $t('trainings.noResults') }}</p>
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
