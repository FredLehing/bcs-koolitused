<script>
import BackLink from '@/components/common/BackLink.vue'
import { mapState } from 'pinia'
import { PhEye } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import UserService from '@/api-services/UserService.js'
import FormatService from '@/services/FormatService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import CheckMark from '@/components/common/CheckMark.vue'
import CourseParticipantStatusBadge from '@/components/common/CourseParticipantStatusBadge.vue'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import UserStatusBadge from '@/components/common/UserStatusBadge.vue'
import UserStatusButton from '@/components/common/UserStatusButton.vue'

const USER_STATUS_DELETED = 'D'

// Üks konto (admin): /admin-user?userId={id} — konto, osaleja ja registreerumised; deaktiveeri / taasta
export default {
  name: 'AdminUserView',
  components: {
    BackLink,
    PhEye,
    CheckMark,
    CourseParticipantStatusBadge,
    InlineAlerts,
    UserStatusBadge,
    UserStatusButton,
  },
  data() {
    return {
      userId: 0,
      user: null,
      successMessage: '',
      errorMessage: '',
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    isDeleted() {
      return this.user.status === USER_STATUS_DELETED
    },

    isOwnAccount() {
      return this.userId === SessionStorageService.getUserId()
    },

    hasParticipant() {
      return this.user.participantId !== null
    },

    // Osaleja e-post näidatakse ainult siis, kui see erineb konto e-postist
    showProfileEmail() {
      return this.hasParticipant && this.user.profileEmail !== this.user.email
    },

    userLabel() {
      return this.user.participantName
        ? `${this.user.participantName} (${this.user.email})`
        : this.user.email
    },
  },
  watch: {
    contentLang() {
      this.getAdminUser()
    },
  },
  methods: {
    getAdminUser() {
      UserService.sendGetAdminUserRequest(this.userId, this.contentLang)
        .then((response) => (this.user = response.data))
        .catch(() => NavigationService.navigateToErrorView())
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
      this.getAdminUser()
    },

    handleStatusError(message) {
      this.successMessage = ''
      this.errorMessage = message
    },

    formatDateTime(instant) {
      return FormatService.formatDateTime(instant)
    },

    formatDateRange(startDate, endDate) {
      return FormatService.formatDateRange(startDate, endDate)
    },
  },
  beforeMount() {
    if (!SessionStorageService.userIsAdmin()) {
      NavigationService.navigateToNotAuthorizedView()
      return
    }
    this.userId = Number(this.$route.query.userId ?? 0)
    if (this.userId === 0) {
      NavigationService.navigateToErrorView()
      return
    }
    this.getAdminUser()
  },
}
</script>

<template>
  <div class="mx-auto w-full max-w-7xl px-6 py-8">
    <BackLink :fallback="{ name: 'adminUsersRoute' }" />
    <div class="flex justify-center">
      <div class="w-full max-w-5xl">
        <template v-if="user">
          <h1 class="text-3xl font-extrabold tracking-tight">{{ $t('adminUser.title') }}</h1>
          <p class="mb-6 mt-1 text-lg text-muted">
            {{ user.email }}
            <UserStatusBadge v-if="isDeleted" :status="user.status" class="ml-1 text-base" />
          </p>

          <section class="mb-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">{{ $t('adminUser.accountLegend') }}</h2>
            <dl class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-[12rem_1fr]">
              <dt class="text-muted">{{ $t('adminUsers.columns.email') }}</dt>
              <dd>{{ user.email }}</dd>
              <dt class="text-muted">{{ $t('adminUsers.columns.roleName') }}</dt>
              <dd>{{ $t(`roles.${user.roleName}`) }}</dd>
              <dt class="text-muted">{{ $t('adminUsers.columns.status') }}</dt>
              <dd><UserStatusBadge :status="user.status" /></dd>
              <dt class="text-muted">{{ $t('adminUsers.columns.createdAt') }}</dt>
              <dd>{{ formatDateTime(user.createdAt) }}</dd>
            </dl>
          </section>

          <section class="mb-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">
              {{ $t('adminUser.participantLegend') }}
            </h2>
            <dl
              v-if="hasParticipant"
              class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-[12rem_1fr]"
            >
              <dt class="text-muted">{{ $t('adminUsers.columns.participantName') }}</dt>
              <dd>{{ user.participantName }}</dd>
              <template v-if="showProfileEmail">
                <dt class="text-muted">{{ $t('adminUsers.columns.email') }}</dt>
                <dd>
                  <a :href="`mailto:${user.profileEmail}`">{{ user.profileEmail }}</a>
                </dd>
              </template>
              <dt class="text-muted">{{ $t('adminUsers.columns.phone') }}</dt>
              <dd>
                <a :href="`tel:${user.phone}`">{{ user.phone }}</a>
              </dd>
            </dl>
            <p v-else class="text-muted">{{ $t('adminUser.noParticipant') }}</p>
          </section>

          <section class="mb-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">
              {{ $t('adminUser.registrationsLegend') }}
            </h2>
            <p v-if="user.registrations.length === 0" class="text-muted">
              {{ $t('adminUser.noRegistrations') }}
            </p>
            <div v-else class="overflow-x-auto">
              <table class="table table-hover">
                <thead>
                  <tr>
                    <th>{{ $t('adminUser.columns.training') }}</th>
                    <th>{{ $t('adminUser.columns.dates') }}</th>
                    <th>{{ $t('adminUser.columns.status') }}</th>
                    <th>{{ $t('adminUser.columns.hasPaid') }}</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="registration in user.registrations"
                    :key="registration.courseParticipantId"
                  >
                    <td>{{ registration.trainingTitle }}</td>
                    <td class="whitespace-nowrap">
                      <RouterLink
                        :to="{
                          name: 'adminCourseRoute',
                          query: { returnTo: $route.fullPath, courseId: registration.courseId },
                        }"
                      >
                        {{ formatDateRange(registration.startDate, registration.endDate) }}
                      </RouterLink>
                      <span v-if="registration.isPast" class="badge text-bg-light ml-1">
                        {{ $t('courseStatus.past') }}
                      </span>
                    </td>
                    <td><CourseParticipantStatusBadge :status="registration.status" /></td>
                    <td><CheckMark :value="registration.hasPaid" /></td>
                    <td>
                      <RouterLink
                        :to="{
                          name: 'adminRegistrationRoute',
                          query: {
                            courseParticipantId: registration.courseParticipantId,
                            returnTo: $route.fullPath,
                          },
                        }"
                        :title="$t('adminUser.viewRegistration')"
                        :aria-label="$t('adminUser.viewRegistration')"
                        class="btn btn-outline-secondary btn-sm btn-icon"
                      >
                        <PhEye :size="20" />
                      </RouterLink>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <div class="mb-6 flex flex-wrap items-center gap-2">
            <p v-if="isOwnAccount" class="text-muted">
              {{ $t('adminUser.ownAccountHint') }}
            </p>
            <UserStatusButton
              v-else
              :user-id="user.userId"
              :user-label="userLabel"
              :status="user.status"
              :deactivate-label="$t('adminUser.deactivate')"
              :restore-label="$t('adminUser.restore')"
              @event-user-deactivated="handleUserDeactivated"
              @event-user-restored="handleUserRestored"
              @event-status-error="handleStatusError"
            />
            <InlineAlerts
              :success-message="successMessage"
              :error-message="errorMessage"
              @event-success-message-closed="successMessage = ''"
              @event-error-message-closed="errorMessage = ''"
            />
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
