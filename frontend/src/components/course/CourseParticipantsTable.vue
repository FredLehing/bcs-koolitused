<script>
import { mapState } from 'pinia'
import { PhEye } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import FormatService from '@/services/FormatService.js'
import SortService from '@/services/SortService.js'
import CheckMark from '@/components/common/CheckMark.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'
import CourseParticipantStatusBadge from '@/components/common/CourseParticipantStatusBadge.vue'

// Toimumiskorra osalejate tabel (AdminCourseView), ainult lugemiseks. Loobunute lüliti ja
// sorteerimine on frontendis (kogu nimekiri on laaditud).
export default {
  name: 'CourseParticipantsTable',
  components: { PhEye, CheckMark, SortableColumnHeader, CourseParticipantStatusBadge },
  props: {
    // Silmaga avatud registreerumisest "← Tagasi" selle toimumiskorra juurde
    courseId: Number,
    courseParticipants: Array,
  },
  data() {
    return {
      includeCancelled: false,
      // Vaikimisi registreerumise järjekorras
      sortBy: 'registeredAt',
      sortDirection: 'asc',
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    sortableColumns() {
      return [
        { sortKey: 'participantName', label: this.$t('adminCourse.participants.columns.name') },
        { sortKey: 'email', label: this.$t('adminCourse.participants.columns.email') },
        { sortKey: 'phone', label: this.$t('adminCourse.participants.columns.phone') },
        {
          sortKey: 'registeredAt',
          label: this.$t('adminCourse.participants.columns.registeredAt'),
        },
        { sortKey: 'hasPaid', label: this.$t('adminCourse.participants.columns.hasPaid') },
        {
          sortKey: 'requiresLaptop',
          label: this.$t('adminCourse.participants.columns.requiresLaptop'),
        },
        { sortKey: 'status', label: this.$t('adminCourse.participants.columns.status') },
      ]
    },

    visibleCourseParticipants() {
      const courseParticipants = this.courseParticipants.filter(
        (courseParticipant) => this.includeCancelled || courseParticipant.status === 'R',
      )
      return SortService.sortRows(
        courseParticipants,
        this.sortBy,
        this.sortDirection,
        this.contentLang,
      )
    },

    // Kokkuvõte loeb ainult registreerunud osalejaid (lülitist sõltumata)
    registeredParticipants() {
      return this.courseParticipants.filter((courseParticipant) => courseParticipant.status === 'R')
    },

    paidCount() {
      return this.registeredParticipants.filter((courseParticipant) => courseParticipant.hasPaid)
        .length
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
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2">
      <h2 class="h4 mb-0">{{ $t('adminCourse.participants.title') }}</h2>
      <div class="form-check form-switch mb-0">
        <input
          v-model="includeCancelled"
          id="includeCancelled"
          class="form-check-input"
          type="checkbox"
          role="switch"
        />
        <label class="form-check-label" for="includeCancelled">
          {{ $t('adminCourse.participants.includeCancelled') }}
        </label>
      </div>
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
            <th>{{ $t('adminCourse.participants.columns.notes') }}</th>
            <th>{{ $t('adminCourse.participants.columns.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="courseParticipant in visibleCourseParticipants"
            :key="courseParticipant.courseParticipantId"
            :class="{ 'cancelled-row': courseParticipant.status === 'C' }"
          >
            <td>{{ courseParticipant.participantName }}</td>
            <td>{{ courseParticipant.email }}</td>
            <td class="text-nowrap">{{ courseParticipant.phone }}</td>
            <td class="text-nowrap">{{ formatDateTime(courseParticipant.registeredAt) }}</td>
            <td><CheckMark :value="courseParticipant.hasPaid" /></td>
            <td><CheckMark :value="courseParticipant.requiresLaptop" /></td>
            <td><CourseParticipantStatusBadge :status="courseParticipant.status" /></td>
            <td class="notes">{{ courseParticipant.notes || '—' }}</td>
            <td>
              <RouterLink
                :to="{
                  name: 'adminRegistrationRoute',
                  query: {
                    courseParticipantId: courseParticipant.courseParticipantId,
                    returnTo: $route.fullPath,
                  },
                }"
                :title="$t('adminCourse.participants.view')"
                :aria-label="$t('adminCourse.participants.view')"
                class="btn btn-sm btn-outline-secondary d-inline-flex"
              >
                <PhEye :size="20" />
              </RouterLink>
            </td>
          </tr>
          <tr v-if="visibleCourseParticipants.length === 0">
            <td colspan="9" class="text-center text-secondary py-4">
              {{ $t('adminCourse.participants.empty') }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="text-secondary">
      {{
        $t('adminCourse.participants.summary', {
          count: registeredParticipants.length,
          paidCount: paidCount,
        })
      }}
    </p>
  </div>
</template>

<style scoped>
/* Loobunud osaleja: tuhmim rida */
.cancelled-row td {
  color: var(--bs-secondary-color);
}

.notes {
  white-space: pre-wrap;
}
</style>
