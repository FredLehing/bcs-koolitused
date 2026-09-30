<script>
import { mapState } from 'pinia'
import { Tooltip } from 'bootstrap'
import { PhCheck, PhEye, PhPlus, PhQuestion, PhX } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import LanguageService from '@/api-services/LanguageService.js'
import CategoryService from '@/api-services/CategoryService.js'
import FundingTypeService from '@/api-services/FundingTypeService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import FlagIcon from '@/components/common/FlagIcon.vue'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import EditTrainingLink from '@/components/common/EditTrainingLink.vue'
import PaginationNav from '@/components/common/PaginationNav.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'
import TrainingDeleteButton from '@/components/common/TrainingDeleteButton.vue'
import TrainingStatusButton from '@/components/common/TrainingStatusButton.vue'
import AdminTrainingFilters from '@/components/forms/AdminTrainingFilters.vue'

const LIMIT = 10
const DEFAULT_SORT_BY = 'createdAt'
const DEFAULT_SORT_DIRECTION = 'desc'

// Filtrite vaikimisi väärtused: ID 0 = kõik, null = filtrit ei rakendata (status null = aktiivsed)
function createDefaultFilters() {
  return {
    categoryId: 0,
    trainingLanguageId: 0,
    fundingTypeId: 0,
    status: null,
    isOrderable: null,
    isPromoted: null,
    hasAllTranslations: null,
  }
}

function padTwoDigits(number) {
  return String(number).padStart(2, '0')
}

export default {
  name: 'AdminTrainingsView',
  components: {
    PhCheck,
    PhEye,
    PhPlus,
    PhQuestion,
    PhX,
    FlagIcon,
    InlineAlerts,
    EditTrainingLink,
    PaginationNav,
    SortableColumnHeader,
    TrainingDeleteButton,
    TrainingStatusButton,
    AdminTrainingFilters,
  },
  data() {
    return {
      successMessage: '',
      errorMessage: '',
      // searchText = väljale sisestatud tekst, appliedSearchText = tekst, millega päring tehti
      searchText: '',
      appliedSearchText: '',
      // filters = kaardil valitud, appliedFilters = päringus kasutatud ("Filtreeri" nupuga)
      filters: createDefaultFilters(),
      appliedFilters: createDefaultFilters(),
      isFilterCardOpen: false,
      sortBy: DEFAULT_SORT_BY,
      sortDirection: DEFAULT_SORT_DIRECTION,
      page: 0,
      totalPages: 0,
      totalElements: 0,
      adminTrainingSummaries: [
        {
          trainingId: 0,
          trainingTranslationId: 0,
          title: '',
          categoryId: 0,
          categoryName: '',
          trainingLanguageCode: '',
          trainingLanguageFlagIconCode: '',
          status: '',
          isOrderable: false,
          isPromoted: false,
          createdAt: '',
          updatedAt: '',
          hasAllTranslations: false,
          missingTranslationLanguageCodes: [],
          fundingTypes: [
            {
              fundingTypeId: 0,
              fundingTypeName: '',
            },
          ],
        },
      ],
      trainingTitles: [
        {
          trainingId: 0,
          title: '',
        },
      ],
      categories: [],
      languages: [],
      fundingTypes: [],
    }
  },
  computed: {
    // Kasutajaliidese keel (navbaris valitud) — nimed, kategooriad ja rahastustüübid selles keeles
    ...mapState(useLanguageStore, ['contentLang']),

    activeFilterCount() {
      const defaultFilters = createDefaultFilters()
      return Object.keys(defaultFilters).filter(
        (filterName) => this.appliedFilters[filterName] !== defaultFilters[filterName],
      ).length
    },

    sortableColumns() {
      return [
        { sortKey: 'createdAt', label: this.$t('adminTrainings.columns.createdAt') },
        { sortKey: 'updatedAt', label: this.$t('adminTrainings.columns.updatedAt') },
        { sortKey: 'title', label: this.$t('adminTrainings.columns.title') },
        { sortKey: 'categoryName', label: this.$t('adminTrainings.columns.category') },
        { sortKey: 'trainingLanguageCode', label: this.$t('adminTrainings.columns.language') },
        { sortKey: 'status', label: this.$t('adminTrainings.columns.status') },
        { sortKey: 'hasAllTranslations', label: this.$t('adminTrainings.columns.translations') },
      ]
    },
  },
  watch: {
    // Keele vahetus navbaris → laadi andmed uues keeles (otsing, filtrid, sorteerimine ja leht jäävad)
    contentLang() {
      this.getCategories()
      this.getFundingTypes()
      this.getTrainingTitles()
      this.getAdminTrainings()
      this.$nextTick(() => this.updateSearchHelpTooltip())
    },
  },
  methods: {
    // ---------- Laadimine ----------

    getAdminTrainings() {
      TrainingService.sendGetAdminTrainingsRequest({
        contentLang: this.contentLang,
        searchText: this.appliedSearchText,
        ...this.appliedFilters,
        sortBy: this.sortBy,
        sortDirection: this.sortDirection,
        page: this.page,
        limit: LIMIT,
      })
        .then((response) => this.handleGetAdminTrainingsResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Kui leht jäi tühjaks (nt viimase rea kustutamise järel), liigutakse eelmisele lehele
    handleGetAdminTrainingsResponse(adminTrainingSummaryDto) {
      if (adminTrainingSummaryDto.adminTrainingSummaries.length === 0 && this.page > 0) {
        this.page = this.page - 1
        this.getAdminTrainings()
        return
      }
      this.totalPages = adminTrainingSummaryDto.totalPages
      this.totalElements = adminTrainingSummaryDto.totalElements
      this.adminTrainingSummaries = adminTrainingSummaryDto.adminTrainingSummaries
    },

    getTrainingTitles() {
      TrainingService.sendGetTrainingTitlesRequest(this.contentLang)
        .then((response) => (this.trainingTitles = response.data))
        .catch(() => NavigationService.navigateToErrorView())
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

    // ---------- Otsing, filtrid, sorteerimine, leheküljestus ----------

    handleSearchClick() {
      this.appliedSearchText = this.searchText.trim()
      this.page = 0
      this.getAdminTrainings()
    },

    handleFilterChanged(filterName, filterValue) {
      this.filters[filterName] = filterValue
    },

    handleFilterClick() {
      this.appliedFilters = { ...this.filters }
      this.page = 0
      this.getAdminTrainings()
    },

    handleClearFiltersClick() {
      this.filters = createDefaultFilters()
      this.appliedFilters = createDefaultFilters()
      this.page = 0
      this.getAdminTrainings()
    },

    // Uus veerg → kasvav, sama veerg uuesti → suund vahetub
    handleSortClick(sortKey) {
      if (this.sortBy === sortKey) {
        this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc'
      } else {
        this.sortBy = sortKey
        this.sortDirection = 'asc'
      }
      this.page = 0
      this.getAdminTrainings()
    },

    handlePageChanged(newPage) {
      this.page = newPage
      this.getAdminTrainings()
    },

    // ---------- Rea tegevused ----------

    navigateToTrainingView(adminTrainingSummary) {
      NavigationService.navigateToTrainingView(
        adminTrainingSummary.trainingId,
        adminTrainingSummary.trainingTranslationId,
      )
    },

    navigateToNewTrainingForm() {
      NavigationService.navigateToTrainingFormView()
    },

    handleTrainingDeleted() {
      this.resetMessages()
      this.successMessage = this.$t('adminTrainings.messages.deleted')
      this.getAdminTrainings()
      this.getTrainingTitles()
    },

    // Taastamise (D → U) järel muutub ka nimede ettepanekute nimekiri
    handleStatusChanged(adminTrainingSummary, newStatus) {
      this.resetMessages()
      this.successMessage = this.statusChangedMessage(adminTrainingSummary.status, newStatus)
      this.getAdminTrainings()
      if (adminTrainingSummary.status === 'D') {
        this.getTrainingTitles()
      }
    },

    handleStatusError(message) {
      this.resetMessages()
      this.errorMessage = message
      this.getAdminTrainings()
    },

    statusChangedMessage(oldStatus, newStatus) {
      if (oldStatus === 'D') {
        return this.$t('adminTrainings.messages.restored')
      }
      return newStatus === 'P'
        ? this.$t('adminTrainings.messages.published')
        : this.$t('adminTrainings.messages.unpublished')
    },

    resetMessages() {
      this.successMessage = ''
      this.errorMessage = ''
    },

    // ---------- Kuvamine ----------

    // Ajatempel (Instant) → 30/09/2026 kasutaja ajavööndis
    formatDate(instant) {
      const date = new Date(instant)
      return `${padTwoDigits(date.getDate())}/${padTwoDigits(date.getMonth() + 1)}/${date.getFullYear()}`
    },

    formatDateTime(instant) {
      return new Date(instant).toLocaleString(this.contentLang)
    },

    statusBadgeClass(status) {
      return {
        U: 'text-bg-secondary',
        P: 'text-bg-success',
        D: 'text-bg-danger',
      }[status]
    },

    trainingSettings(adminTrainingSummary) {
      const settings = adminTrainingSummary.fundingTypes.map(
        (fundingType) => fundingType.fundingTypeName,
      )
      if (adminTrainingSummary.isOrderable) {
        settings.push(this.$t('adminTrainings.settings.orderable'))
      }
      if (adminTrainingSummary.isPromoted) {
        settings.push(this.$t('adminTrainings.settings.promoted'))
      }
      return settings
    },

    missingTranslationsText(adminTrainingSummary) {
      return this.$t('adminTrainings.translationsMissing', {
        languages: adminTrainingSummary.missingTranslationLanguageCodes.join(', '),
      })
    },

    // Bootstrap tooltip loeb teksti ainult loomisel, keele vahetusel tuleb see uuendada
    updateSearchHelpTooltip() {
      this.searchHelpTooltip.setContent({ '.tooltip-inner': this.$t('adminTrainings.searchHelp') })
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.getLanguages()
      this.getCategories()
      this.getFundingTypes()
      this.getTrainingTitles()
      this.getAdminTrainings()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
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
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
      <h1 class="h3 mb-0">{{ $t('adminTrainings.title') }}</h1>
      <button
        @click="navigateToNewTrainingForm"
        class="btn btn-primary d-inline-flex align-items-center gap-1"
        type="button"
      >
        <PhPlus :size="18" />
        {{ $t('navbar.addTraining') }}
      </button>
    </div>

    <div class="d-flex align-items-center gap-2 mb-2">
      <div class="input-group">
        <input
          v-model="searchText"
          @keyup.enter="handleSearchClick"
          :placeholder="$t('adminTrainings.searchPlaceholder')"
          :aria-label="$t('adminTrainings.searchPlaceholder')"
          list="admin-training-titles"
          autocomplete="off"
          class="form-control"
          type="text"
        />
        <button @click="handleSearchClick" class="btn btn-primary" type="button">
          {{ $t('adminTrainings.search') }}
        </button>
      </div>
      <datalist id="admin-training-titles">
        <option
          v-for="trainingTitle in trainingTitles"
          :key="trainingTitle.trainingId"
          :value="trainingTitle.title"
        ></option>
      </datalist>
      <span
        ref="searchHelp"
        class="text-secondary"
        role="img"
        tabindex="0"
        data-bs-toggle="tooltip"
        data-bs-placement="left"
        :data-bs-title="$t('adminTrainings.searchHelp')"
        :aria-label="$t('adminTrainings.searchHelp')"
      >
        <PhQuestion :size="22" />
      </span>
    </div>

    <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
      <button
        @click="isFilterCardOpen = !isFilterCardOpen"
        :aria-expanded="isFilterCardOpen"
        aria-controls="admin-training-filters"
        class="btn btn-link p-0"
        type="button"
      >
        {{
          isFilterCardOpen ? $t('adminTrainings.filters.hide') : $t('adminTrainings.filters.show')
        }}
      </button>
      <template v-if="activeFilterCount > 0">
        <span class="badge text-bg-primary">
          {{ $t('adminTrainings.filters.activeCount', activeFilterCount) }}
        </span>
        <button @click="handleClearFiltersClick" class="btn btn-link btn-sm p-0" type="button">
          {{ $t('adminTrainings.filters.clear') }}
        </button>
      </template>
    </div>

    <AdminTrainingFilters
      v-if="isFilterCardOpen"
      :filters="filters"
      :categories="categories"
      :languages="languages"
      :funding-types="fundingTypes"
      @event-filter-changed="handleFilterChanged"
      @event-filter-clicked="handleFilterClick"
      @event-clear-clicked="handleClearFiltersClick"
    />

    <div class="mb-2">
      <InlineAlerts
        :success-message="successMessage"
        :error-message="errorMessage"
        @event-success-message-closed="successMessage = ''"
        @event-error-message-closed="errorMessage = ''"
      />
    </div>

    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead>
          <tr>
            <SortableColumnHeader
              v-for="sortableColumn in sortableColumns"
              :key="sortableColumn.sortKey"
              :label="sortableColumn.label"
              :sort-key="sortableColumn.sortKey"
              :sort-by="sortBy"
              :sort-direction="sortDirection"
              @event-sort-clicked="handleSortClick"
            />
            <th>{{ $t('adminTrainings.columns.settings') }}</th>
            <th>{{ $t('adminTrainings.columns.actions') }}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="adminTrainingSummary in adminTrainingSummaries"
            :key="adminTrainingSummary.trainingId"
            :class="{ 'deleted-row': adminTrainingSummary.status === 'D' }"
          >
            <td class="text-nowrap" :title="formatDateTime(adminTrainingSummary.createdAt)">
              {{ formatDate(adminTrainingSummary.createdAt) }}
            </td>
            <td class="text-nowrap" :title="formatDateTime(adminTrainingSummary.updatedAt)">
              {{ formatDate(adminTrainingSummary.updatedAt) }}
            </td>
            <td>{{ adminTrainingSummary.title }}</td>
            <td>{{ adminTrainingSummary.categoryName }}</td>
            <td>
              <FlagIcon
                :flag-icon-code="adminTrainingSummary.trainingLanguageFlagIconCode"
                :title="adminTrainingSummary.trainingLanguageCode"
                class="fs-5"
              />
            </td>
            <td>
              <span class="badge" :class="statusBadgeClass(adminTrainingSummary.status)">
                {{ $t(`adminTrainings.status.${adminTrainingSummary.status}`) }}
              </span>
            </td>
            <td>
              <PhCheck
                v-if="adminTrainingSummary.hasAllTranslations"
                :size="20"
                weight="bold"
                class="text-success"
                :aria-label="$t('adminTrainings.translationsComplete')"
              />
              <span
                v-else
                :title="missingTranslationsText(adminTrainingSummary)"
                :aria-label="missingTranslationsText(adminTrainingSummary)"
                role="img"
              >
                <PhX :size="20" weight="bold" class="text-danger" />
              </span>
            </td>
            <td>
              <ul class="settings-list list-unstyled mb-0 small">
                <li
                  v-for="setting in trainingSettings(adminTrainingSummary)"
                  :key="setting"
                  class="text-nowrap"
                >
                  {{ setting }}
                </li>
                <li v-if="trainingSettings(adminTrainingSummary).length === 0" class="no-settings">
                  —
                </li>
              </ul>
            </td>
            <td>
              <div v-if="adminTrainingSummary.status !== 'D'" class="d-flex gap-1">
                <button
                  @click="navigateToTrainingView(adminTrainingSummary)"
                  :title="$t('adminTrainings.view')"
                  :aria-label="$t('adminTrainings.view')"
                  class="btn btn-sm btn-outline-secondary d-inline-flex"
                  type="button"
                >
                  <PhEye :size="20" />
                </button>
                <EditTrainingLink
                  :training-id="adminTrainingSummary.trainingId"
                  :training-translation-id="adminTrainingSummary.trainingTranslationId"
                />
                <TrainingDeleteButton
                  :training-id="adminTrainingSummary.trainingId"
                  :title="adminTrainingSummary.title"
                  @event-training-deleted="handleTrainingDeleted"
                />
              </div>
            </td>
            <td class="text-nowrap">
              <TrainingStatusButton
                :training-id="adminTrainingSummary.trainingId"
                :status="adminTrainingSummary.status"
                :title="adminTrainingSummary.title"
                button-class="btn btn-sm btn-outline-primary"
                @event-status-changed="handleStatusChanged(adminTrainingSummary, $event)"
                @event-status-error="handleStatusError"
              />
            </td>
          </tr>
          <tr v-if="adminTrainingSummaries.length === 0">
            <td colspan="10" class="text-center text-secondary py-4">
              {{ $t('adminTrainings.noResults') }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-4">
      <span class="text-secondary">
        {{ $t('adminTrainings.totalCount', totalElements) }}
      </span>
      <PaginationNav
        :page="page"
        :total-pages="totalPages"
        @event-page-changed="handlePageChanged"
      />
      <span></span>
    </div>
  </div>
</template>

<style scoped>
/* "Sätted" veerg: iga säte sinise täpiga */
.settings-list li::before {
  content: '';
  display: inline-block;
  width: 0.45rem;
  height: 0.45rem;
  margin-right: 0.4rem;
  border-radius: 50%;
  background-color: var(--bs-primary);
  vertical-align: middle;
}

.settings-list li.no-settings::before {
  display: none;
}

/* Kustutatud koolitus: tuhmim rida, ainult "Taasta" nupp */
.deleted-row td {
  color: var(--bs-secondary-color);
}

.deleted-row td :deep(.fi),
.deleted-row .settings-list {
  opacity: 0.55;
}
</style>
