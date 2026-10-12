<script>
import { mapState } from 'pinia'
import {
  PhCalendarBlank,
  PhEye,
  PhFunnel,
  PhMagnifyingGlass,
  PhPencilSimple,
  PhStar,
} from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import CourseService from '@/api-services/CourseService.js'
import TrainingService from '@/api-services/TrainingService.js'
import LanguageService from '@/api-services/LanguageService.js'
import CategoryService from '@/api-services/CategoryService.js'
import FormatService from '@/services/FormatService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import AdminTabs from '@/components/common/AdminTabs.vue'
import CheckMark from '@/components/common/CheckMark.vue'
import CourseDeleteButton from '@/components/common/CourseDeleteButton.vue'
import CourseStatusBadge from '@/components/common/CourseStatusBadge.vue'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import PaginationNav from '@/components/common/PaginationNav.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'
import AdminCourseFilters from '@/components/forms/AdminCourseFilters.vue'

const LIMIT = 10

// Filtrite vaikimisi väärtused: ID 0 = kõik, null / '' = filtrit ei rakendata
function createDefaultFilters() {
  return {
    startDateFrom: '',
    startDateTo: '',
    categoryId: 0,
    trainingLanguageId: 0,
    status: null,
    attendance: null,
    isPromoted: null,
  }
}

export default {
  name: 'AdminAllCoursesView',
  components: {
    AdminTabs,
    PhCalendarBlank,
    PhEye,
    PhFunnel,
    PhMagnifyingGlass,
    PhPencilSimple,
    PhStar,
    CheckMark,
    CourseDeleteButton,
    CourseStatusBadge,
    InlineAlerts,
    PaginationNav,
    SortableColumnHeader,
    AdminCourseFilters,
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
      includePast: false,
      // null = vaikimisi järjestus (tulevased lähimast, siis möödunud hiliseimast)
      sortBy: null,
      sortDirection: 'asc',
      page: 0,
      totalPages: 0,
      totalElements: 0,
      adminCourseSummaries: [
        {
          courseId: 0,
          trainingId: 0,
          trainingTitle: '',
          startDate: '',
          endDate: '',
          isPast: false,
          numberOfDays: 0,
          price: 0,
          status: '',
          isPromoted: false,
          hasMeetingLink: false,
          participantCount: 0,
          paidCount: 0,
          enquiryCount: 0,
        },
      ],
      trainingTitles: [],
      categories: [],
      languages: [],
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    activeFilterCount() {
      const defaultFilters = createDefaultFilters()
      return Object.keys(defaultFilters).filter(
        (filterName) =>
          (this.appliedFilters[filterName] ?? '') !== (defaultFilters[filterName] ?? ''),
      ).length
    },

    sortableColumns() {
      return {
        startDate: this.$t('adminAllCourses.columns.startDate'),
        trainingTitle: this.$t('adminAllCourses.columns.training'),
        price: this.$t('adminAllCourses.columns.price'),
        status: this.$t('adminAllCourses.columns.status'),
        participantCount: this.$t('adminAllCourses.columns.participants'),
        enquiryCount: this.$t('adminAllCourses.columns.enquiries'),
      }
    },
  },
  watch: {
    // Keele vahetus → tabel, nimede ettepanekud ja kategooriad uues keeles (filtrid, sorteerimine ja leht jäävad)
    contentLang() {
      this.getCategories()
      this.getTrainingTitles()
      this.getAdminCourses()
    },

    includePast() {
      this.page = 0
      this.getAdminCourses()
    },
  },
  methods: {
    // ---------- Laadimine ----------

    getAdminCourses() {
      CourseService.sendGetAdminCoursesRequest({
        contentLang: this.contentLang,
        searchText: this.appliedSearchText,
        categoryId: this.appliedFilters.categoryId,
        trainingLanguageId: this.appliedFilters.trainingLanguageId,
        status: this.appliedFilters.status,
        attendance: this.appliedFilters.attendance,
        isPromoted: this.appliedFilters.isPromoted,
        startDateFrom: this.appliedFilters.startDateFrom || null,
        startDateTo: this.appliedFilters.startDateTo || null,
        includePast: this.includePast,
        sortBy: this.sortBy,
        sortDirection: this.sortDirection,
        page: this.page,
        limit: LIMIT,
      })
        .then((response) => this.handleGetAdminCoursesResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Kui leht jäi tühjaks (nt viimase rea kustutamise järel), liigutakse eelmisele lehele
    handleGetAdminCoursesResponse(adminCourseSummaryDto) {
      if (adminCourseSummaryDto.adminCourseSummaries.length === 0 && this.page > 0) {
        this.page = this.page - 1
        this.getAdminCourses()
        return
      }
      this.totalPages = adminCourseSummaryDto.totalPages
      this.totalElements = adminCourseSummaryDto.totalElements
      this.adminCourseSummaries = adminCourseSummaryDto.adminCourseSummaries
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

    // ---------- Otsing, filtrid, sorteerimine, leheküljestus ----------

    handleSearchClick() {
      this.appliedSearchText = this.searchText.trim()
      this.page = 0
      this.getAdminCourses()
    },

    handleFilterChanged(filterName, filterValue) {
      this.filters[filterName] = filterValue
    },

    handleFilterClick() {
      this.appliedFilters = { ...this.filters }
      this.page = 0
      this.getAdminCourses()
    },

    handleClearFiltersClick() {
      this.filters = createDefaultFilters()
      this.appliedFilters = createDefaultFilters()
      this.page = 0
      this.getAdminCourses()
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
      this.getAdminCourses()
    },

    handlePageChanged(newPage) {
      this.page = newPage
      this.getAdminCourses()
    },

    handleCourseDeleted() {
      this.errorMessage = ''
      this.successMessage = this.$t('adminTrainingCourses.messages.deleted')
      this.getAdminCourses()
    },

    // ---------- Kuvamine ----------

    formatLocalDate(localDate) {
      return FormatService.formatLocalDate(localDate)
    },

    formatPrice(price) {
      return FormatService.formatPrice(price, this.contentLang)
    },

    // "tasunud / osalejad"; osalejateta "—"
    paidText(adminCourseSummary) {
      if (adminCourseSummary.participantCount === 0) {
        return '—'
      }
      return `${adminCourseSummary.paidCount} / ${adminCourseSummary.participantCount}`
    },

    isAllPaid(adminCourseSummary) {
      return (
        adminCourseSummary.participantCount > 0 &&
        adminCourseSummary.paidCount === adminCourseSummary.participantCount
      )
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.getLanguages()
      this.getCategories()
      this.getTrainingTitles()
      this.getAdminCourses()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
  },
}
</script>

<template>
  <div class="mx-auto w-full max-w-7xl px-6 py-8">
    <AdminTabs />

    <div class="mb-6 flex items-end justify-between gap-4">
      <h1 class="text-3xl font-extrabold tracking-tight">{{ $t('adminAllCourses.title') }}</h1>
      <span class="text-muted">{{ $t('adminAllCourses.totalCount', totalElements) }}</span>
    </div>

    <div class="mb-4 flex items-center gap-2">
      <form class="flex flex-1 gap-2" @submit.prevent="handleSearchClick">
        <div
          class="flex min-h-11 flex-1 items-center gap-2 rounded-lg border border-brand-200 bg-white px-3 focus-within:border-brand-600 focus-within:ring-3 focus-within:ring-brand-600/15"
        >
          <PhMagnifyingGlass :size="18" class="shrink-0 text-muted" />
          <input
            v-model="searchText"
            :placeholder="$t('adminAllCourses.searchPlaceholder')"
            :aria-label="$t('adminAllCourses.searchPlaceholder')"
            list="admin-course-training-titles"
            autocomplete="off"
            class="min-w-0 flex-1 bg-transparent outline-none"
            type="text"
          />
        </div>
        <button class="btn btn-primary" type="submit">
          {{ $t('adminAllCourses.search') }}
        </button>
      </form>
      <button
        @click="isFilterCardOpen = !isFilterCardOpen"
        :aria-expanded="isFilterCardOpen"
        aria-controls="admin-course-filters"
        :class="{ 'border-brand-600 bg-brand-50': isFilterCardOpen }"
        class="btn btn-outline-secondary"
        type="button"
      >
        <PhFunnel :size="18" />
        {{ $t('adminAllCourses.filters.title') }}
        <span v-if="activeFilterCount > 0" class="badge text-bg-primary">
          {{ activeFilterCount }}
        </span>
      </button>
      <button
        v-if="activeFilterCount > 0"
        @click="handleClearFiltersClick"
        class="btn btn-link btn-sm"
        type="button"
      >
        {{ $t('adminAllCourses.filters.clear') }}
      </button>
    </div>
    <datalist id="admin-course-training-titles">
      <option
        v-for="trainingTitle in trainingTitles"
        :key="trainingTitle.trainingId"
        :value="trainingTitle.title"
      ></option>
    </datalist>

    <AdminCourseFilters
      v-if="isFilterCardOpen"
      :filters="filters"
      :categories="categories"
      :languages="languages"
      @event-filter-changed="handleFilterChanged"
      @event-filter-clicked="handleFilterClick"
      @event-clear-clicked="handleClearFiltersClick"
    />

    <div class="overflow-hidden rounded-2xl border border-line bg-white">
      <div class="flex items-center justify-between gap-3 border-b border-line px-4 py-3">
        <div class="form-check form-switch">
          <input
            v-model="includePast"
            id="includePast"
            class="form-check-input"
            type="checkbox"
            role="switch"
          />
          <label class="form-check-label" for="includePast">
            {{ $t('adminTrainingCourses.includePast') }}
          </label>
        </div>
        <InlineAlerts
          :success-message="successMessage"
          :error-message="errorMessage"
          @event-success-message-closed="successMessage = ''"
          @event-error-message-closed="errorMessage = ''"
        />
      </div>

      <div class="overflow-x-auto">
        <table class="table table-hover">
          <thead class="bg-surface">
            <tr>
              <SortableColumnHeader
                :label="sortableColumns.startDate"
                sort-key="startDate"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <th>{{ $t('adminAllCourses.columns.numberOfDays') }}</th>
              <SortableColumnHeader
                :label="sortableColumns.trainingTitle"
                sort-key="trainingTitle"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <SortableColumnHeader
                :label="sortableColumns.price"
                sort-key="price"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <SortableColumnHeader
                :label="sortableColumns.status"
                sort-key="status"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <SortableColumnHeader
                :label="sortableColumns.participantCount"
                sort-key="participantCount"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <th>{{ $t('adminAllCourses.columns.paid') }}</th>
              <th>{{ $t('adminAllCourses.columns.meetingLink') }}</th>
              <SortableColumnHeader
                :label="sortableColumns.enquiryCount"
                sort-key="enquiryCount"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <th class="text-right">{{ $t('adminAllCourses.columns.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="adminCourseSummary in adminCourseSummaries"
              :key="adminCourseSummary.courseId"
              :class="{ 'text-muted': adminCourseSummary.isPast }"
            >
              <td class="font-semibold whitespace-nowrap tabular-nums">
                {{ formatLocalDate(adminCourseSummary.startDate) }}
              </td>
              <td class="tabular-nums">{{ adminCourseSummary.numberOfDays }}</td>
              <td>
                <span class="inline-flex items-center gap-1.5">
                  <PhStar
                    v-if="adminCourseSummary.isPromoted"
                    :size="16"
                    weight="fill"
                    class="shrink-0 text-brand-600"
                    :title="$t('adminAllCourses.promoted')"
                    :aria-label="$t('adminAllCourses.promoted')"
                  />
                  <RouterLink
                    :to="{
                      name: 'adminTrainingCoursesRoute',
                      query: {
                        returnTo: $route.fullPath,
                        trainingId: adminCourseSummary.trainingId,
                      },
                    }"
                    class="font-semibold"
                  >
                    {{ adminCourseSummary.trainingTitle }}
                  </RouterLink>
                </span>
              </td>
              <td class="whitespace-nowrap tabular-nums">
                {{ formatPrice(adminCourseSummary.price) }} €
              </td>
              <td>
                <CourseStatusBadge
                  :status="adminCourseSummary.status"
                  :is-past="adminCourseSummary.isPast"
                />
              </td>
              <td class="tabular-nums">{{ adminCourseSummary.participantCount }}</td>
              <td
                class="whitespace-nowrap tabular-nums"
                :class="{ 'font-bold text-emerald-700': isAllPaid(adminCourseSummary) }"
              >
                {{ paidText(adminCourseSummary) }}
              </td>
              <td><CheckMark :value="adminCourseSummary.hasMeetingLink" /></td>
              <td class="tabular-nums">{{ adminCourseSummary.enquiryCount }}</td>
              <td>
                <div class="flex justify-end gap-1">
                  <RouterLink
                    :to="{
                      name: 'adminCourseRoute',
                      query: { returnTo: $route.fullPath, courseId: adminCourseSummary.courseId },
                    }"
                    :title="$t('adminAllCourses.view')"
                    :aria-label="$t('adminAllCourses.view')"
                    class="btn btn-outline-secondary btn-sm btn-icon"
                  >
                    <PhEye :size="18" />
                  </RouterLink>
                  <RouterLink
                    :to="{
                      name: 'courseFormRoute',
                      query: { returnTo: $route.fullPath, courseId: adminCourseSummary.courseId },
                    }"
                    :title="$t('adminTrainingCourses.edit')"
                    :aria-label="$t('adminTrainingCourses.edit')"
                    class="btn btn-outline-secondary btn-sm btn-icon"
                  >
                    <PhPencilSimple :size="18" />
                  </RouterLink>
                  <RouterLink
                    :to="{
                      name: 'adminTrainingCoursesRoute',
                      query: {
                        returnTo: $route.fullPath,
                        trainingId: adminCourseSummary.trainingId,
                      },
                    }"
                    :title="$t('adminTrainings.calendar')"
                    :aria-label="$t('adminTrainings.calendar')"
                    class="btn btn-outline-secondary btn-sm btn-icon"
                  >
                    <PhCalendarBlank :size="18" />
                  </RouterLink>
                  <CourseDeleteButton
                    :course-id="adminCourseSummary.courseId"
                    :start-date="adminCourseSummary.startDate"
                    :end-date="adminCourseSummary.endDate"
                    :participant-count="adminCourseSummary.participantCount"
                    @event-course-deleted="handleCourseDeleted"
                  />
                </div>
              </td>
            </tr>
            <tr v-if="adminCourseSummaries.length === 0">
              <td colspan="10" class="py-10 text-center text-muted">
                {{ $t('adminAllCourses.noResults') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <PaginationNav
      :page="page"
      :total-pages="totalPages"
      @event-page-changed="handlePageChanged"
      class="mt-6"
    />
  </div>
</template>
