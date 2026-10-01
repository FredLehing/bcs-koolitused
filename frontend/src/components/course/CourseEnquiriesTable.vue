<script>
import { mapState } from 'pinia'
import { PhEye } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import FormatService from '@/services/FormatService.js'
import SortService from '@/services/SortService.js'
import EnquiryStatusBadge from '@/components/common/EnquiryStatusBadge.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'

// Toimumiskorraga seotud päringute tabel (AdminCourseView), ainult lugemiseks. Sorteerimine on
// frontendis (kogu nimekiri on laaditud).
export default {
  name: 'CourseEnquiriesTable',
  components: { PhEye, EnquiryStatusBadge, SortableColumnHeader },
  props: {
    courseEnquiries: Array,
  },
  data() {
    return {
      // Vaikimisi uusimad eespool
      sortBy: 'createdAt',
      sortDirection: 'desc',
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    sortableColumns() {
      return [
        { sortKey: 'createdAt', label: this.$t('adminCourse.enquiries.columns.createdAt') },
        { sortKey: 'fullName', label: this.$t('adminCourse.enquiries.columns.name') },
        { sortKey: 'email', label: this.$t('adminCourse.enquiries.columns.email') },
        { sortKey: 'companyName', label: this.$t('adminCourse.enquiries.columns.company') },
        { sortKey: 'status', label: this.$t('adminCourse.enquiries.columns.status') },
      ]
    },

    sortedCourseEnquiries() {
      return SortService.sortRows(
        this.courseEnquiries,
        this.sortBy,
        this.sortDirection,
        this.contentLang,
      )
    },
  },
  methods: {
    // Uus veerg → kasvav, sama veerg uuesti → suund vahetub
    handleSortClick(sortKey) {
      if (this.sortBy === sortKey) {
        this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc'
      } else {
        this.sortBy = sortKey
        this.sortDirection = 'asc'
      }
    },

    formatDateTime(instant) {
      return FormatService.formatDateTime(instant)
    },
  },
}
</script>

<template>
  <div>
    <h2 class="h4 mb-2">{{ $t('adminCourse.enquiries.title') }}</h2>

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
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="courseEnquiry in sortedCourseEnquiries" :key="courseEnquiry.enquiryId">
            <td class="text-nowrap">{{ formatDateTime(courseEnquiry.createdAt) }}</td>
            <td>{{ courseEnquiry.fullName }}</td>
            <td>{{ courseEnquiry.email }}</td>
            <td>{{ courseEnquiry.companyName ?? '—' }}</td>
            <td><EnquiryStatusBadge :status="courseEnquiry.status" /></td>
            <td>
              <RouterLink
                :to="{
                  name: 'adminEnquiryRoute',
                  query: { returnTo: $route.fullPath, enquiryId: courseEnquiry.enquiryId },
                }"
                :title="$t('adminCourse.enquiries.view')"
                :aria-label="$t('adminCourse.enquiries.view')"
                class="btn btn-sm btn-outline-secondary d-inline-flex"
              >
                <PhEye :size="20" />
              </RouterLink>
            </td>
          </tr>
          <tr v-if="sortedCourseEnquiries.length === 0">
            <td colspan="6" class="text-center text-secondary py-4">
              {{ $t('adminCourse.enquiries.empty') }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
