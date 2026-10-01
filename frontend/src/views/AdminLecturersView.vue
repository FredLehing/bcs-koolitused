<script>
import { mapState } from 'pinia'
import { PhCheck, PhPencilSimple, PhPlus, PhX } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import LecturerService from '@/api-services/LecturerService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import AdminTabs from '@/components/common/AdminTabs.vue'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import LecturerDeleteButton from '@/components/common/LecturerDeleteButton.vue'
import LecturerRestoreButton from '@/components/common/LecturerRestoreButton.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'

// Sorteeritava veeru väärtus võrdlemiseks (tekstid võrreldakse keele järgi, nt Õ pärast O-d)
const SORT_VALUES = {
  fullName: (lecturer) => lecturer.fullName,
  title: (lecturer) => lecturer.title ?? '',
  hasAllTranslations: (lecturer) => (lecturer.hasAllTranslations ? 1 : 0),
  trainingCount: (lecturer) => lecturer.trainingCount,
  upcomingCourseCount: (lecturer) => lecturer.upcomingCourseCount,
  updatedAt: (lecturer) => lecturer.updatedAt,
}

function padTwoDigits(number) {
  return String(number).padStart(2, '0')
}

export default {
  name: 'AdminLecturersView',
  components: {
    AdminTabs,
    PhCheck,
    PhPencilSimple,
    PhPlus,
    PhX,
    InlineAlerts,
    LecturerDeleteButton,
    LecturerRestoreButton,
    SortableColumnHeader,
  },
  data() {
    return {
      successMessage: '',
      errorMessage: '',
      searchText: '',
      includeDeleted: false,
      // null = backendi järjestus (nime järgi)
      sortBy: null,
      sortDirection: 'asc',
      lecturers: [
        {
          lecturerId: 0,
          lecturerTranslationId: 0,
          fullName: '',
          title: '',
          status: '',
          hasAllTranslations: false,
          missingTranslationLanguageCodes: [],
          trainingCount: 0,
          upcomingCourseCount: 0,
          updatedAt: '',
        },
      ],
    }
  },
  computed: {
    // Kasutajaliidese keel (navbaris valitud) — ametinimetus ja "Muuda" tõlge selles keeles
    ...mapState(useLanguageStore, ['contentLang']),

    // Otsing frontendis: nimi sisaldab otsingusõna (tõstutundetu)
    filteredLecturers() {
      const search = this.searchText.trim().toLowerCase()
      return this.lecturers.filter((lecturer) => lecturer.fullName.toLowerCase().includes(search))
    },

    // Sorteerimine ainult frontendis; võrdsete väärtuste korral jääb nime järjekord (stabiilne sort)
    sortedLecturers() {
      if (this.sortBy === null) {
        return this.filteredLecturers
      }
      const sortValue = SORT_VALUES[this.sortBy]
      const direction = this.sortDirection === 'asc' ? 1 : -1
      return [...this.filteredLecturers].sort(
        (a, b) => this.compareValues(sortValue(a), sortValue(b)) * direction,
      )
    },

    sortableColumns() {
      return [
        { sortKey: 'fullName', label: this.$t('adminLecturers.columns.fullName') },
        { sortKey: 'title', label: this.$t('adminLecturers.columns.title') },
        { sortKey: 'hasAllTranslations', label: this.$t('adminLecturers.columns.translations') },
        { sortKey: 'trainingCount', label: this.$t('adminLecturers.columns.trainingCount') },
        {
          sortKey: 'upcomingCourseCount',
          label: this.$t('adminLecturers.columns.upcomingCourseCount'),
        },
        { sortKey: 'updatedAt', label: this.$t('adminLecturers.columns.updatedAt') },
      ]
    },
  },
  watch: {
    // Keele vahetus navbaris → nimekiri uues keeles (otsing ja lüliti jäävad)
    contentLang() {
      this.getAdminLecturers()
    },

    includeDeleted() {
      this.getAdminLecturers()
    },
  },
  methods: {
    getAdminLecturers() {
      LecturerService.sendGetAdminLecturersRequest(this.contentLang, this.includeDeleted)
        .then((response) => (this.lecturers = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // 1. klõps kasvav → 2. kahanev → 3. vaikimisi järjestus (nime järgi)
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

    navigateToNewLecturerForm() {
      NavigationService.navigateToLecturerFormView({})
    },

    handleLecturerDeleted() {
      this.resetMessages()
      this.successMessage = this.$t('adminLecturers.messages.deleted')
      this.getAdminLecturers()
    },

    handleDeleteError(message) {
      this.resetMessages()
      this.errorMessage = message
      this.getAdminLecturers()
    },

    handleLecturerRestored() {
      this.resetMessages()
      this.successMessage = this.$t('adminLecturers.messages.restored')
      this.getAdminLecturers()
    },

    resetMessages() {
      this.successMessage = ''
      this.errorMessage = ''
    },

    // Ajatempel (Instant) → 30/09/2026 kasutaja ajavööndis
    formatDate(instant) {
      if (!instant) {
        return ''
      }
      const date = new Date(instant)
      return `${padTwoDigits(date.getDate())}/${padTwoDigits(date.getMonth() + 1)}/${date.getFullYear()}`
    },

    missingTranslationsText(lecturer) {
      return this.$t('adminTrainings.translationsMissing', {
        languages: lecturer.missingTranslationLanguageCodes.join(', '),
      })
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.getAdminLecturers()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
  },
}
</script>

<template>
  <div class="container">
    <AdminTabs />

    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
      <h1 class="h3 mb-0">{{ $t('adminLecturers.title') }}</h1>
      <button
        @click="navigateToNewLecturerForm"
        class="btn btn-primary d-inline-flex align-items-center gap-1"
        type="button"
      >
        <PhPlus :size="18" />
        {{ $t('navbar.addLecturer') }}
      </button>
    </div>

    <div class="d-flex flex-wrap align-items-center gap-3 mb-3">
      <input
        v-model="searchText"
        :placeholder="$t('adminLecturers.searchPlaceholder')"
        :aria-label="$t('adminLecturers.searchPlaceholder')"
        class="form-control search-input"
        type="search"
      />
      <div class="form-check form-switch mb-0">
        <input
          v-model="includeDeleted"
          id="includeDeleted"
          class="form-check-input"
          type="checkbox"
          role="switch"
        />
        <label class="form-check-label" for="includeDeleted">
          {{ $t('adminLecturers.showDeleted') }}
        </label>
      </div>
    </div>

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
            <th>{{ $t('adminLecturers.columns.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="lecturer in sortedLecturers"
            :key="lecturer.lecturerId"
            :class="{ 'deleted-row': lecturer.status === 'D' }"
          >
            <td>
              {{ lecturer.fullName }}
              <span v-if="lecturer.status === 'D'" class="badge text-bg-danger ms-1">
                {{ $t('adminLecturers.deleted') }}
              </span>
            </td>
            <td>{{ lecturer.title }}</td>
            <td>
              <PhCheck
                v-if="lecturer.hasAllTranslations"
                :size="20"
                weight="bold"
                class="text-success"
                :aria-label="$t('adminTrainings.translationsComplete')"
              />
              <span
                v-else
                :title="missingTranslationsText(lecturer)"
                :aria-label="missingTranslationsText(lecturer)"
                role="img"
              >
                <PhX :size="20" weight="bold" class="text-danger" />
              </span>
            </td>
            <td>{{ lecturer.trainingCount }}</td>
            <td>{{ lecturer.upcomingCourseCount }}</td>
            <td class="text-nowrap">{{ formatDate(lecturer.updatedAt) }}</td>
            <td>
              <div v-if="lecturer.status !== 'D'" class="d-flex gap-1">
                <RouterLink
                  :to="{
                    name: 'lecturerFormRoute',
                    query: {
                      lecturerId: lecturer.lecturerId,
                      lecturerTranslationId: lecturer.lecturerTranslationId,
                    },
                  }"
                  :title="$t('adminLecturers.edit')"
                  :aria-label="$t('adminLecturers.edit')"
                  class="btn btn-sm btn-outline-secondary d-inline-flex"
                >
                  <PhPencilSimple :size="20" />
                </RouterLink>
                <LecturerDeleteButton
                  :lecturer-id="lecturer.lecturerId"
                  :full-name="lecturer.fullName"
                  :upcoming-course-count="lecturer.upcomingCourseCount"
                  @event-lecturer-deleted="handleLecturerDeleted"
                  @event-delete-error="handleDeleteError"
                />
              </div>
              <LecturerRestoreButton
                v-else
                :lecturer-id="lecturer.lecturerId"
                :full-name="lecturer.fullName"
                @event-lecturer-restored="handleLecturerRestored"
              />
            </td>
          </tr>
          <tr v-if="filteredLecturers.length === 0">
            <td colspan="7" class="text-center text-secondary py-4">
              {{ $t('adminLecturers.noResults') }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="text-secondary mb-4">
      {{ $t('adminLecturers.totalCount', filteredLecturers.length) }}
    </p>
  </div>
</template>

<style scoped>
.search-input {
  max-width: 24rem;
}

/* Kustutatud koolitaja: tuhmim rida, ainult "Taasta" nupp */
.deleted-row td {
  color: var(--bs-secondary-color);
}
</style>
