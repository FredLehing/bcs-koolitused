<script>
import { mapState } from 'pinia'
import { PhPencilSimple, PhPlus } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import RoomService from '@/api-services/RoomService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import AdminTabs from '@/components/common/AdminTabs.vue'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import RoomDeleteButton from '@/components/common/RoomDeleteButton.vue'
import RoomRestoreButton from '@/components/common/RoomRestoreButton.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'

// Sorteeritava veeru väärtus võrdlemiseks (tekstid võrreldakse keele järgi, nt Õ pärast O-d)
const SORT_VALUES = {
  roomName: (room) => room.roomName,
  upcomingCourseCount: (room) => room.upcomingCourseCount,
  courseCount: (room) => room.courseCount,
  updatedAt: (room) => room.updatedAt,
}

function padTwoDigits(number) {
  return String(number).padStart(2, '0')
}

export default {
  name: 'AdminRoomsView',
  components: {
    AdminTabs,
    PhPencilSimple,
    PhPlus,
    InlineAlerts,
    RoomDeleteButton,
    RoomRestoreButton,
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
      rooms: [
        {
          roomId: 0,
          roomName: '',
          status: '',
          upcomingCourseCount: 0,
          courseCount: 0,
          updatedAt: '',
        },
      ],
    }
  },
  computed: {
    // Kasutajaliidese keel — tekstide võrdlemiseks sorteerimisel
    ...mapState(useLanguageStore, ['contentLang']),

    // Otsing frontendis: nimi sisaldab otsingusõna (tõstutundetu)
    filteredRooms() {
      const search = this.searchText.trim().toLowerCase()
      return this.rooms.filter((room) => room.roomName.toLowerCase().includes(search))
    },

    // Sorteerimine ainult frontendis; võrdsete väärtuste korral jääb nime järjekord (stabiilne sort)
    sortedRooms() {
      if (this.sortBy === null) {
        return this.filteredRooms
      }
      const sortValue = SORT_VALUES[this.sortBy]
      const direction = this.sortDirection === 'asc' ? 1 : -1
      return [...this.filteredRooms].sort(
        (a, b) => this.compareValues(sortValue(a), sortValue(b)) * direction,
      )
    },

    sortableColumns() {
      return [
        { sortKey: 'roomName', label: this.$t('adminRooms.columns.roomName') },
        {
          sortKey: 'upcomingCourseCount',
          label: this.$t('adminRooms.columns.upcomingCourseCount'),
        },
        { sortKey: 'courseCount', label: this.$t('adminRooms.columns.courseCount') },
        { sortKey: 'updatedAt', label: this.$t('adminRooms.columns.updatedAt') },
      ]
    },
  },
  watch: {
    includeDeleted() {
      this.getAdminRooms()
    },
  },
  methods: {
    getAdminRooms() {
      RoomService.sendGetAdminRoomsRequest(this.includeDeleted)
        .then((response) => (this.rooms = response.data))
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

    navigateToNewRoomForm() {
      NavigationService.navigateToRoomFormView({})
    },

    handleRoomDeleted() {
      this.resetMessages()
      this.successMessage = this.$t('adminRooms.messages.deleted')
      this.getAdminRooms()
    },

    handleDeleteError(message) {
      this.resetMessages()
      this.errorMessage = message
      this.getAdminRooms()
    },

    handleRoomRestored() {
      this.resetMessages()
      this.successMessage = this.$t('adminRooms.messages.restored')
      this.getAdminRooms()
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
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      // Vormist tulles eduteade ("Ruum lisatud" / "Salvestatud")
      this.successMessage = window.history.state?.successMessage ?? ''
      this.getAdminRooms()
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
      <h1 class="h3 mb-0">{{ $t('adminRooms.title') }}</h1>
      <button
        @click="navigateToNewRoomForm"
        class="btn btn-primary d-inline-flex align-items-center gap-1"
        type="button"
      >
        <PhPlus :size="18" />
        {{ $t('adminRooms.addRoom') }}
      </button>
    </div>

    <div class="d-flex flex-wrap align-items-center gap-3 mb-3">
      <input
        v-model="searchText"
        :placeholder="$t('adminRooms.searchPlaceholder')"
        :aria-label="$t('adminRooms.searchPlaceholder')"
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
          {{ $t('adminRooms.showDeleted') }}
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
            <th>{{ $t('adminRooms.columns.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="room in sortedRooms"
            :key="room.roomId"
            :class="{ 'deleted-row': room.status === 'D' }"
          >
            <td>
              {{ room.roomName }}
              <span v-if="room.status === 'D'" class="badge text-bg-danger ms-1">
                {{ $t('adminRooms.deleted') }}
              </span>
            </td>
            <td>{{ room.upcomingCourseCount }}</td>
            <td>{{ room.courseCount }}</td>
            <td class="text-nowrap">{{ formatDate(room.updatedAt) }}</td>
            <td>
              <div v-if="room.status !== 'D'" class="d-flex gap-1">
                <RouterLink
                  :to="{ name: 'roomFormRoute', query: { roomId: room.roomId } }"
                  :title="$t('adminRooms.edit')"
                  :aria-label="$t('adminRooms.edit')"
                  class="btn btn-sm btn-outline-secondary d-inline-flex"
                >
                  <PhPencilSimple :size="20" />
                </RouterLink>
                <RoomDeleteButton
                  :room-id="room.roomId"
                  :room-name="room.roomName"
                  :upcoming-course-count="room.upcomingCourseCount"
                  @event-room-deleted="handleRoomDeleted"
                  @event-delete-error="handleDeleteError"
                />
              </div>
              <RoomRestoreButton
                v-else
                :room-id="room.roomId"
                :room-name="room.roomName"
                @event-room-restored="handleRoomRestored"
              />
            </td>
          </tr>
          <tr v-if="filteredRooms.length === 0">
            <td colspan="5" class="text-center text-secondary py-4">
              {{ $t('adminRooms.noResults') }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="text-secondary mb-4">
      {{ $t('adminRooms.totalCount', filteredRooms.length) }}
    </p>
  </div>
</template>

<style scoped>
.search-input {
  max-width: 24rem;
}

/* Kustutatud ruum: tuhmim rida, ainult "Taasta" nupp */
.deleted-row td {
  color: var(--bs-secondary-color);
}
</style>
