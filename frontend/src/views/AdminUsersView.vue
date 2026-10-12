<script>
import { mapState } from 'pinia'
import { PhEye, PhMagnifyingGlass } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import UserService from '@/api-services/UserService.js'
import FormatService from '@/services/FormatService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import SortService from '@/services/SortService.js'
import AdminTabs from '@/components/common/AdminTabs.vue'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import PaginationNav from '@/components/common/PaginationNav.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'
import UserStatusBadge from '@/components/common/UserStatusBadge.vue'
import UserStatusButton from '@/components/common/UserStatusButton.vue'

const USER_STATUS_DELETED = 'D'
const LIMIT = 10

// Kõik kontod (admin): /admin-users. Otsing, rolli filter, sorteerimine ja leheküljestus frontendis;
// "Näita ka deaktiveeritud" → uus päring (includeDeleted)
export default {
  name: 'AdminUsersView',
  components: {
    PhEye,
    PhMagnifyingGlass,
    AdminTabs,
    InlineAlerts,
    PaginationNav,
    SortableColumnHeader,
    UserStatusBadge,
    UserStatusButton,
  },
  data() {
    return {
      successMessage: '',
      errorMessage: '',
      searchText: '',
      // '' = kõik rollid, 'participant' / 'admin'
      roleName: '',
      includeDeleted: false,
      // null = backendi järjestus (loodud, uusimad eespool)
      sortBy: null,
      sortDirection: 'asc',
      page: 0,
      currentUserId: SessionStorageService.getUserId(),
      users: [],
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    // Otsing: e-post, nimi või telefon sisaldab otsingusõna (tõstutundetu)
    filteredUsers() {
      const search = this.searchText.trim().toLowerCase()
      return this.users.filter(
        (user) =>
          (this.roleName === '' || user.roleName === this.roleName) &&
          [user.email, user.participantName, user.phone].some((value) =>
            (value ?? '').toLowerCase().includes(search),
          ),
      )
    },

    sortedUsers() {
      if (this.sortBy === null) {
        return this.filteredUsers
      }
      return SortService.sortRows(
        this.filteredUsers,
        this.sortBy,
        this.sortDirection,
        this.contentLang,
      )
    },

    totalPages() {
      return Math.ceil(this.sortedUsers.length / LIMIT)
    },

    pagedUsers() {
      return this.sortedUsers.slice(this.page * LIMIT, (this.page + 1) * LIMIT)
    },

    sortableColumns() {
      return [
        { sortKey: 'createdAt', label: this.$t('adminUsers.columns.createdAt') },
        { sortKey: 'email', label: this.$t('adminUsers.columns.email') },
        { sortKey: 'participantName', label: this.$t('adminUsers.columns.participantName') },
      ]
    },

    sortableColumnsAfterPhone() {
      return [
        { sortKey: 'roleName', label: this.$t('adminUsers.columns.roleName') },
        { sortKey: 'registrationCount', label: this.$t('adminUsers.columns.registrationCount') },
        { sortKey: 'status', label: this.$t('adminUsers.columns.status') },
      ]
    },
  },
  watch: {
    includeDeleted() {
      this.page = 0
      this.getAdminUsers()
    },

    searchText() {
      this.page = 0
    },

    roleName() {
      this.page = 0
    },

    // Nt viimase lehe ainsa konto deaktiveerimisel jääks leht muidu tühjaks
    totalPages(newTotalPages) {
      if (this.page > 0 && this.page >= newTotalPages) {
        this.page = newTotalPages - 1
      }
    },
  },
  methods: {
    getAdminUsers() {
      UserService.sendGetAdminUsersRequest(this.includeDeleted)
        .then((response) => (this.users = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // 1. klõps kasvav → 2. kahanev → 3. vaikimisi järjestus (loodud, uusimad eespool)
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
      this.page = 0
    },

    handlePageChanged(newPage) {
      this.page = newPage
    },

    handleUserDeactivated() {
      this.showSuccessMessage(this.$t('adminUsers.messages.deactivated'))
    },

    handleUserRestored() {
      this.showSuccessMessage(this.$t('adminUsers.messages.restored'))
    },

    showSuccessMessage(message) {
      this.errorMessage = ''
      this.successMessage = message
      this.getAdminUsers()
    },

    handleStatusError(message) {
      this.successMessage = ''
      this.errorMessage = message
      this.getAdminUsers()
    },

    isDeleted(user) {
      return user.status === USER_STATUS_DELETED
    },

    userLabel(user) {
      return user.participantName ? `${user.participantName} (${user.email})` : user.email
    },

    formatDateTime(instant) {
      return FormatService.formatDateTime(instant)
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.getAdminUsers()
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
      <h1 class="text-3xl font-extrabold tracking-tight">{{ $t('adminUsers.title') }}</h1>
      <span class="text-muted">{{ $t('adminUsers.totalCount', filteredUsers.length) }}</span>
    </div>

    <div class="mb-4 flex flex-wrap items-center gap-x-6 gap-y-3">
      <div
        class="flex min-h-11 max-w-xl flex-1 items-center gap-2 rounded-lg border border-brand-200 bg-white px-3 focus-within:border-brand-600 focus-within:ring-3 focus-within:ring-brand-600/15"
      >
        <PhMagnifyingGlass :size="18" class="shrink-0 text-muted" />
        <input
          v-model="searchText"
          :placeholder="$t('adminUsers.searchPlaceholder')"
          :aria-label="$t('adminUsers.searchPlaceholder')"
          class="min-w-0 flex-1 bg-transparent outline-none"
          type="search"
        />
      </div>
      <select
        v-model="roleName"
        :aria-label="$t('adminUsers.columns.roleName')"
        class="form-select w-auto"
      >
        <option value="">{{ $t('adminUsers.allRoles') }}</option>
        <option value="participant">{{ $t('adminUsers.participants') }}</option>
        <option value="admin">{{ $t('adminUsers.admins') }}</option>
      </select>
      <div class="form-check form-switch">
        <input
          v-model="includeDeleted"
          id="includeDeleted"
          class="form-check-input"
          type="checkbox"
          role="switch"
        />
        <label class="form-check-label" for="includeDeleted">
          {{ $t('adminUsers.showDeleted') }}
        </label>
      </div>
    </div>

    <div class="mb-3">
      <InlineAlerts
        :success-message="successMessage"
        :error-message="errorMessage"
        @event-success-message-closed="successMessage = ''"
        @event-error-message-closed="errorMessage = ''"
      />
    </div>

    <div class="overflow-hidden rounded-2xl border border-line bg-white">
      <div class="overflow-x-auto">
        <table class="table table-hover">
          <thead class="bg-surface">
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
              <th>{{ $t('adminUsers.columns.phone') }}</th>
              <SortableColumnHeader
                v-for="sortableColumn in sortableColumnsAfterPhone"
                :key="sortableColumn.sortKey"
                :label="sortableColumn.label"
                :sort-key="sortableColumn.sortKey"
                :sort-by="sortBy"
                :sort-direction="sortDirection"
                @event-sort-clicked="handleSortClick"
              />
              <th class="text-right">{{ $t('adminUsers.columns.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="user in pagedUsers"
              :key="user.userId"
              :class="{ 'deleted-row': isDeleted(user) }"
            >
              <td class="whitespace-nowrap">{{ formatDateTime(user.createdAt) }}</td>
              <td>
                {{ user.email }}
                <span v-if="user.userId === currentUserId" class="badge text-bg-primary ml-1">
                  {{ $t('adminUsers.me') }}
                </span>
              </td>
              <td>{{ user.participantName ?? '—' }}</td>
              <td class="whitespace-nowrap">{{ user.phone ?? '—' }}</td>
              <td>{{ $t(`roles.${user.roleName}`) }}</td>
              <td>{{ user.registrationCount }}</td>
              <td><UserStatusBadge :status="user.status" /></td>
              <td class="text-right">
                <div class="inline-flex gap-2">
                  <RouterLink
                    :to="{
                      name: 'adminUserRoute',
                      query: { returnTo: $route.fullPath, userId: user.userId },
                    }"
                    :title="$t('adminUsers.view')"
                    :aria-label="$t('adminUsers.view')"
                    class="btn btn-sm btn-icon btn-outline-secondary"
                  >
                    <PhEye :size="20" />
                  </RouterLink>
                  <UserStatusButton
                    v-if="user.userId !== currentUserId"
                    :user-id="user.userId"
                    :user-label="userLabel(user)"
                    :status="user.status"
                    @event-user-deactivated="handleUserDeactivated"
                    @event-user-restored="handleUserRestored"
                    @event-status-error="handleStatusError"
                  />
                </div>
              </td>
            </tr>
            <tr v-if="filteredUsers.length === 0">
              <td colspan="8" class="py-10 text-center text-muted">
                {{ $t('adminUsers.noResults') }}
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

<style scoped>
/* Tuhmim rida */
.deleted-row td {
  color: var(--color-muted);
}
</style>
