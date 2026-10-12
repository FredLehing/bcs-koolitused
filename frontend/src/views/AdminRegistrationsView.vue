<script>
import { mapState } from 'pinia'
import { PhEye, PhMagnifyingGlass } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import CourseParticipantService from '@/api-services/CourseParticipantService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import FormatService from '@/services/FormatService.js'
import CheckMark from '@/components/common/CheckMark.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'
import AdminTabs from '@/components/common/AdminTabs.vue'
import CourseParticipantStatusBadge from '@/components/common/CourseParticipantStatusBadge.vue'

// Staatuse kasvav järjekord sorteerimisel: Registreerunud → Loobunud
const STATUS_ORDER = { R: 1, C: 2 }

// Sorteeritava veeru väärtus võrdlemiseks (tekstid võrreldakse keele järgi, nt Õ pärast O-d)
const SORT_VALUES = {
  registeredAt: (registration) => registration.registeredAt,
  participantName: (registration) => registration.participantName,
  trainingTitle: (registration) => registration.trainingTitle,
  courseStartDate: (registration) => registration.courseStartDate,
  hasPaid: (registration) => registration.hasPaid,
  requiresLaptop: (registration) => registration.requiresLaptop,
  status: (registration) => STATUS_ORDER[registration.status],
}

export default {
  name: 'AdminRegistrationsView',
  components: {
    AdminTabs,
    PhEye,
    PhMagnifyingGlass,
    CheckMark,
    SortableColumnHeader,
    CourseParticipantStatusBadge,
  },
  data() {
    return {
      searchText: '',
      includeCancelled: false,
      includePast: false,
      // null = backendi järjestus (uusimad eespool)
      sortBy: null,
      sortDirection: 'asc',
      registrations: [
        {
          courseParticipantId: 0,
          registeredAt: '',
          participantName: '',
          email: '',
          courseId: 0,
          trainingTitle: '',
          courseStartDate: '',
          courseEndDate: '',
          isPast: false,
          hasPaid: false,
          requiresLaptop: false,
          status: '',
        },
      ],
    }
  },
  computed: {
    // Kasutajaliidese keel — koolituse nimi selles keeles
    ...mapState(useLanguageStore, ['contentLang']),

    // Otsing frontendis: osaleja nimi, e-post või koolitus sisaldab otsingusõna (tõstutundetu)
    filteredRegistrations() {
      const search = this.searchText.trim().toLowerCase()
      return this.registrations.filter((registration) =>
        [registration.participantName, registration.email, registration.trainingTitle].some(
          (value) => value.toLowerCase().includes(search),
        ),
      )
    },

    // Sorteerimine ainult frontendis; võrdsete väärtuste korral jääb backendi järjekord (stabiilne sort)
    sortedRegistrations() {
      if (this.sortBy === null) {
        return this.filteredRegistrations
      }
      const sortValue = SORT_VALUES[this.sortBy]
      const direction = this.sortDirection === 'asc' ? 1 : -1
      return [...this.filteredRegistrations].sort(
        (a, b) => this.compareValues(sortValue(a), sortValue(b)) * direction,
      )
    },

    sortableColumns() {
      return {
        registeredAt: this.$t('adminRegistrations.columns.registeredAt'),
        participantName: this.$t('adminRegistrations.columns.participantName'),
        trainingTitle: this.$t('adminRegistrations.columns.trainingTitle'),
        courseStartDate: this.$t('adminRegistrations.columns.courseDates'),
        hasPaid: this.$t('adminRegistrations.columns.hasPaid'),
        requiresLaptop: this.$t('adminRegistrations.columns.requiresLaptop'),
        status: this.$t('adminRegistrations.columns.status'),
      }
    },
  },
  watch: {
    // Keele vahetus navbaris → nimekiri uues keeles (otsing ja lülitid jäävad)
    contentLang() {
      this.getAdminRegistrations()
    },

    includeCancelled() {
      this.getAdminRegistrations()
    },

    includePast() {
      this.getAdminRegistrations()
    },
  },
  methods: {
    getAdminRegistrations() {
      CourseParticipantService.sendGetAdminRegistrationsRequest(
        this.contentLang,
        this.includeCancelled,
        this.includePast,
      )
        .then((response) => (this.registrations = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // 1. klõps kasvav → 2. kahanev → 3. vaikimisi järjestus (uusimad eespool)
    handleSortClick(sortKey) {
      if (this.sortBy !== sortKey) {
        this.sortBy = sortKey
        this.sortDirection = 'asc'
      } else if (this.sortDirection === 'asc') {
        this.sortDirection = 'desc'
      } else {
        this.sortBy = null
        this.sortDirection = 'asc'
      }
    },

    compareValues(valueA, valueB) {
      if (typeof valueA === 'string') {
        return valueA.localeCompare(valueB, this.contentLang)
      }
      return valueA - valueB
    },

    formatDateTime(instant) {
      return FormatService.formatDateTime(instant)
    },

    formatDateRange(registration) {
      return FormatService.formatDateRange(registration.courseStartDate, registration.courseEndDate)
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.getAdminRegistrations()
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
      <h1 class="text-3xl font-extrabold tracking-tight">{{ $t('adminRegistrations.title') }}</h1>
      <span class="text-muted">{{
        $t('adminRegistrations.totalCount', filteredRegistrations.length)
      }}</span>
    </div>

    <div class="mb-4 flex flex-wrap items-center gap-x-6 gap-y-3">
      <div
        class="flex min-h-11 max-w-xl flex-1 items-center gap-2 rounded-lg border border-brand-200 bg-white px-3 focus-within:border-brand-600 focus-within:ring-3 focus-within:ring-brand-600/15"
      >
        <PhMagnifyingGlass :size="18" class="shrink-0 text-muted" />
        <input
          v-model="searchText"
          :placeholder="$t('adminRegistrations.searchPlaceholder')"
          :aria-label="$t('adminRegistrations.searchPlaceholder')"
          class="min-w-0 flex-1 bg-transparent outline-none"
          type="search"
        />
      </div>
      <div class="form-check form-switch">
        <input
          v-model="includeCancelled"
          id="includeCancelled"
          class="form-check-input"
          type="checkbox"
          role="switch"
        />
        <label class="form-check-label" for="includeCancelled">
          {{ $t('adminRegistrations.showCancelled') }}
        </label>
      </div>
      <div class="form-check form-switch">
        <input
          v-model="includePast"
          id="includePast"
          class="form-check-input"
          type="checkbox"
          role="switch"
        />
        <label class="form-check-label" for="includePast">
          {{ $t('adminRegistrations.showPast') }}
        </label>
      </div>
    </div>

    <div class="overflow-hidden rounded-2xl border border-line bg-white">
      <div class="overflow-x-auto">
        <table class="table table-hover">
          <thead class="bg-surface">
            <tr>
              <SortableColumnHeader
                :label="sortableColumns.registeredAt"
                sort-key="registeredAt"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <SortableColumnHeader
                :label="sortableColumns.participantName"
                sort-key="participantName"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <th>{{ $t('adminRegistrations.columns.email') }}</th>
              <SortableColumnHeader
                :label="sortableColumns.trainingTitle"
                sort-key="trainingTitle"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <SortableColumnHeader
                :label="sortableColumns.courseStartDate"
                sort-key="courseStartDate"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <SortableColumnHeader
                :label="sortableColumns.hasPaid"
                sort-key="hasPaid"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <SortableColumnHeader
                :label="sortableColumns.requiresLaptop"
                sort-key="requiresLaptop"
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
              <th class="text-right">{{ $t('adminRegistrations.columns.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="registration in sortedRegistrations"
              :key="registration.courseParticipantId"
              :class="{ 'cancelled-row': registration.status === 'C' }"
            >
              <td class="whitespace-nowrap">{{ formatDateTime(registration.registeredAt) }}</td>
              <td>{{ registration.participantName }}</td>
              <td>{{ registration.email }}</td>
              <td>{{ registration.trainingTitle }}</td>
              <td class="whitespace-nowrap">
                <RouterLink
                  :to="{
                    name: 'adminCourseRoute',
                    query: { returnTo: $route.fullPath, courseId: registration.courseId },
                  }"
                >
                  {{ formatDateRange(registration) }}
                </RouterLink>
                <span v-if="registration.isPast" class="badge text-bg-light ml-1">
                  {{ $t('courseStatus.past') }}
                </span>
              </td>
              <td><CheckMark :value="registration.hasPaid" /></td>
              <td><CheckMark :value="registration.requiresLaptop" /></td>
              <td><CourseParticipantStatusBadge :status="registration.status" /></td>
              <td>
                <div class="flex justify-end">
                  <RouterLink
                    :to="{
                      name: 'adminRegistrationRoute',
                      query: {
                        returnTo: $route.fullPath,
                        courseParticipantId: registration.courseParticipantId,
                      },
                    }"
                    :title="$t('adminRegistrations.view')"
                    :aria-label="$t('adminRegistrations.view')"
                    class="btn btn-sm btn-icon btn-outline-secondary"
                  >
                    <PhEye :size="20" />
                  </RouterLink>
                </div>
              </td>
            </tr>
            <tr v-if="filteredRegistrations.length === 0">
              <td colspan="9" class="py-10 text-center text-muted">
                {{ $t('adminRegistrations.noResults') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Loobunud registreerumine: tuhmim rida */
.cancelled-row td {
  color: var(--color-muted);
}
</style>
