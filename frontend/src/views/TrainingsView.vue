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
  },
  data() {
    return {
      categoryId: 0,
      fundingTypeId: 0,
      limit: 4,
      page: 0,
      trainingLanguageId: 0,
      // searchText = väljale sisestatud tekst, appliedSearchText = tekst, millega päring tehti
      searchText: '',
      appliedSearchText: '',
      totalPages: 0,
      totalElements: 0,
      trainings: [
        {
          trainingId: 0,
          trainingLanguageCode: '',
          trainingLanguageFlagIconCode: '',
          title: '',
          shortDescription: '',
          categoryId: 0,
          categoryName: '',
          isOrderable: false,
          isPromoted: false,
          fundingTypes: [
            {
              fundingTypeId: 0,
              fundingTypeName: '',
            },
          ],
        },
      ],
      categories: [
        {
          categoryId: 0,
          categoryName: '',
        },
      ],
      languages: [
        {
          languageId: 0,
          languageName: '',
        },
      ],
      fundingTypes: [
        {
          fundingTypeId: 0,
          fundingTypeName: '',
        },
      ],
    }
  },
  computed: {
    // Kasutajaliidese keel (navbaris valitud) — sellega küsitakse koolituste tõlgitud väljad
    ...mapState(useLanguageStore, ['contentLang']),
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
    getTrainings() {
      TrainingService.sendGetTrainingsRequest(
        this.categoryId,
        this.fundingTypeId,
        this.limit,
        this.page,
        this.trainingLanguageId,
        this.contentLang,
        this.appliedSearchText,
      )
        .then((response) => this.handleGetTrainingsResponse(response))
        .catch(() => NavigationService.navigateToErrorView())
        .finally()
    },
    handleGetTrainingsResponse(response) {
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
      this.searchHelpTooltip.setContent({ '.tooltip-inner': this.$t('trainings.searchHelp') })
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
      CategoryService.sendGetCategoriesRequest(this.contentLang)
        .then((response) => (this.categories = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },
    getFundingTypes() {
      FundingTypeService.sendGetFundingTypesRequest(this.contentLang)
        .then((response) => (this.fundingTypes = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },
    handleNewLanguageSelected(newLanguageId) {
      this.trainingLanguageId = newLanguageId
      this.page = 0
      this.getTrainings()
    },
    handleNewCategorySelected(newCategoryId) {
      this.categoryId = newCategoryId
      this.page = 0
      this.getTrainings()
    },

    handleNewFundingTypeSelected(newFundingTypeId) {
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
    this.searchHelpTooltip.dispose()
  },
}
</script>

<template>
  <div class="container d-flex flex-grow-1 flex-column">
    <div class="row flex-grow-1">
      <div class="col-2 d-flex flex-column gap-3">
        <LanguagesDropdown
          :languages="languages"
          :languageId="trainingLanguageId"
          @event-new-language-selected="handleNewLanguageSelected"
          :firstOptionLabel="$t('trainings.showAllLanguages')"
        />
        <CategoriesDropdown
          :categories="categories"
          :categoryId="categoryId"
          @event-new-category-selected="handleNewCategorySelected"
          :firstOptionLabel="$t('trainings.showAllCategories')"
        />
        <FundingTypesRadio
          :fundingTypes="fundingTypes"
          :fundingTypeId="fundingTypeId"
          @event-new-fundingtype-selected="handleNewFundingTypeSelected"
          :firstOptionLabel="$t('trainings.showAllFundingTypes')"
        />
      </div>
      <div class="col-10 d-flex flex-column">
        <div class="d-flex align-items-center gap-2 mb-3">
          <div class="input-group">
            <input
              ref="searchInput"
              v-model="searchText"
              type="text"
              class="form-control"
              :placeholder="$t('trainings.searchPlaceholder')"
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
          v-if="trainings.length === 0"
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
