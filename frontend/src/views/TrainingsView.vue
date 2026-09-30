<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import NavigationService from '@/services/NavigationService.js'
import TrainingCard from '@/components/TrainingCard.vue'
import PaginationNav from '@/components/common/PaginationNav.vue'
import { PhQuestion, PhX } from '@phosphor-icons/vue'
import { Tooltip } from 'bootstrap'

export default {
  name: 'TrainingsView',
  components: { TrainingCard, PaginationNav, PhQuestion, PhX },
  data() {
    return {
      categoryId: 0,
      fundingTypeId: 0,
      limit: 3,
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
  },
  beforeMount() {
    this.getTrainings()
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
  <div class="container">
    <div class="row">
      <div class="col-2">Siin on filtrid</div>
      <div class="col-10">
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
          class="mb-3"
        />
      </div>
    </div>
  </div>
</template>
