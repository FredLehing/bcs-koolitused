<script>
import { mapState } from 'pinia'
import { PhEye } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import EnquiryService from '@/api-services/EnquiryService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import FormatService from '@/services/FormatService.js'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'
import EnquiryStatusBadge from '@/components/common/EnquiryStatusBadge.vue'

// Staatuse kasvav järjekord sorteerimisel: Uus → Käsitletud
const STATUS_ORDER = { U: 1, H: 2 }

// Sorteeritava veeru väärtus võrdlemiseks (tekstid võrreldakse keele järgi, nt Õ pärast O-d)
const SORT_VALUES = {
  createdAt: (enquiry) => enquiry.createdAt,
  fullName: (enquiry) => enquiry.fullName,
  trainingTitle: (enquiry) => enquiry.trainingTitle,
  status: (enquiry) => STATUS_ORDER[enquiry.status],
}

export default {
  name: 'AdminEnquiriesView',
  components: { PhEye, SortableColumnHeader, EnquiryStatusBadge },
  data() {
    return {
      searchText: '',
      includeHandled: false,
      // null = backendi järjestus (uusimad eespool)
      sortBy: null,
      sortDirection: 'asc',
      enquiries: [
        {
          enquiryId: 0,
          createdAt: '',
          fullName: '',
          email: '',
          companyName: null,
          trainingTitle: '',
          courseStartDate: null,
          courseEndDate: null,
          status: '',
        },
      ],
    }
  },
  computed: {
    // Kasutajaliidese keel — koolituse nimi selles keeles
    ...mapState(useLanguageStore, ['contentLang']),

    // Otsing frontendis: nimi, e-post, ettevõte või koolitus sisaldab otsingusõna (tõstutundetu)
    filteredEnquiries() {
      const search = this.searchText.trim().toLowerCase()
      return this.enquiries.filter((enquiry) =>
        [enquiry.fullName, enquiry.email, enquiry.companyName ?? '', enquiry.trainingTitle].some(
          (value) => value.toLowerCase().includes(search),
        ),
      )
    },

    // Sorteerimine ainult frontendis; võrdsete väärtuste korral jääb backendi järjekord (stabiilne sort)
    sortedEnquiries() {
      if (this.sortBy === null) {
        return this.filteredEnquiries
      }
      const sortValue = SORT_VALUES[this.sortBy]
      const direction = this.sortDirection === 'asc' ? 1 : -1
      return [...this.filteredEnquiries].sort(
        (a, b) => this.compareValues(sortValue(a), sortValue(b)) * direction,
      )
    },

    sortableColumns() {
      return {
        createdAt: this.$t('adminEnquiries.columns.createdAt'),
        fullName: this.$t('adminEnquiries.columns.fullName'),
        trainingTitle: this.$t('adminEnquiries.columns.trainingTitle'),
        status: this.$t('adminEnquiries.columns.status'),
      }
    },
  },
  watch: {
    // Keele vahetus navbaris → nimekiri uues keeles (otsing ja lüliti jäävad)
    contentLang() {
      this.getAdminEnquiries()
    },

    includeHandled() {
      this.getAdminEnquiries()
    },
  },
  methods: {
    getAdminEnquiries() {
      EnquiryService.sendGetAdminEnquiriesRequest(this.contentLang, this.includeHandled)
        .then((response) => (this.enquiries = response.data))
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

    formatCourse(enquiry) {
      return FormatService.formatDateRange(enquiry.courseStartDate, enquiry.courseEndDate) || '—'
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.getAdminEnquiries()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
  },
}
</script>

<template>
  <div class="container">
    <!-- PROTOTÜÜP: päringute ja registreerumiste vaheline lülitus vahelehtedena -->
    <ul class="nav nav-tabs mb-3">
      <li class="nav-item">
        <RouterLink
          :to="{ name: 'adminEnquiriesRoute' }"
          class="nav-link active"
          aria-current="page"
        >
          {{ $t('navbar.manageEnquiries') }}
        </RouterLink>
      </li>
      <li class="nav-item">
        <RouterLink :to="{ name: 'adminRegistrationsRoute' }" class="nav-link">
          {{ $t('navbar.manageRegistrations') }}
        </RouterLink>
      </li>
    </ul>

    <h1 class="h3 mb-3">{{ $t('adminEnquiries.title') }}</h1>

    <div class="d-flex flex-wrap align-items-center gap-3 mb-3">
      <input
        v-model="searchText"
        :placeholder="$t('adminEnquiries.searchPlaceholder')"
        :aria-label="$t('adminEnquiries.searchPlaceholder')"
        class="form-control search-input"
        type="search"
      />
      <div class="form-check form-switch mb-0">
        <input
          v-model="includeHandled"
          id="includeHandled"
          class="form-check-input"
          type="checkbox"
          role="switch"
        />
        <label class="form-check-label" for="includeHandled">
          {{ $t('adminEnquiries.showHandled') }}
        </label>
      </div>
    </div>

    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead>
          <tr>
            <SortableColumnHeader
              :label="sortableColumns.createdAt"
              sort-key="createdAt"
              :sort-by="sortBy"
              :sort-direction="sortDirection"
              @event-sort-clicked="handleSortClick"
            />
            <SortableColumnHeader
              :label="sortableColumns.fullName"
              sort-key="fullName"
              :sort-by="sortBy"
              :sort-direction="sortDirection"
              @event-sort-clicked="handleSortClick"
            />
            <th>{{ $t('adminEnquiries.columns.email') }}</th>
            <th>{{ $t('adminEnquiries.columns.companyName') }}</th>
            <SortableColumnHeader
              :label="sortableColumns.trainingTitle"
              sort-key="trainingTitle"
              :sort-by="sortBy"
              :sort-direction="sortDirection"
              @event-sort-clicked="handleSortClick"
            />
            <th>{{ $t('adminEnquiries.columns.course') }}</th>
            <SortableColumnHeader
              :label="sortableColumns.status"
              sort-key="status"
              :sort-by="sortBy"
              :sort-direction="sortDirection"
              @event-sort-clicked="handleSortClick"
            />
            <th>{{ $t('adminEnquiries.columns.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="enquiry in sortedEnquiries"
            :key="enquiry.enquiryId"
            :class="{ 'fw-semibold': enquiry.status === 'U' }"
          >
            <td class="text-nowrap">{{ formatDateTime(enquiry.createdAt) }}</td>
            <td>{{ enquiry.fullName }}</td>
            <td>{{ enquiry.email }}</td>
            <td>{{ enquiry.companyName ?? '—' }}</td>
            <td>{{ enquiry.trainingTitle }}</td>
            <td class="text-nowrap">{{ formatCourse(enquiry) }}</td>
            <td><EnquiryStatusBadge :status="enquiry.status" /></td>
            <td>
              <RouterLink
                :to="{ name: 'adminEnquiryRoute', query: { enquiryId: enquiry.enquiryId } }"
                :title="$t('adminEnquiries.view')"
                :aria-label="$t('adminEnquiries.view')"
                class="btn btn-sm btn-outline-secondary d-inline-flex"
              >
                <PhEye :size="20" />
              </RouterLink>
            </td>
          </tr>
          <tr v-if="filteredEnquiries.length === 0">
            <td colspan="8" class="text-center text-secondary py-4">
              {{ $t('adminEnquiries.noResults') }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="text-secondary mb-4">
      {{ $t('adminEnquiries.totalCount', filteredEnquiries.length) }}
    </p>
  </div>
</template>

<style scoped>
.search-input {
  max-width: 28rem;
}
</style>
