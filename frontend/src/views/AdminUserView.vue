<script>
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

    // AdminRegistrationView tagasilink viib siia tagasi
    returnTo() {
      return `/admin-user?userId=${this.userId}`
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
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-lg-9 text-start">
        <RouterLink :to="{ name: 'adminUsersRoute' }" class="d-inline-block mb-3">
          ← {{ $t('adminUser.backToUsers') }}
        </RouterLink>

        <template v-if="user">
          <h1 class="mb-1">{{ $t('adminUser.title') }}</h1>
          <p class="text-secondary fs-5 mb-3">
            {{ user.email }}
            <UserStatusBadge v-if="isDeleted" :status="user.status" class="fs-6 ms-1" />
          </p>

          <fieldset class="border rounded bg-body p-3 mb-4">
            <legend class="float-none w-auto px-2 fs-5">{{ $t('adminUser.accountLegend') }}</legend>
            <dl class="row mb-0">
              <dt class="col-sm-4">{{ $t('adminUsers.columns.email') }}</dt>
              <dd class="col-sm-8">{{ user.email }}</dd>
              <dt class="col-sm-4">{{ $t('adminUsers.columns.roleName') }}</dt>
              <dd class="col-sm-8">{{ $t(`roles.${user.roleName}`) }}</dd>
              <dt class="col-sm-4">{{ $t('adminUsers.columns.status') }}</dt>
              <dd class="col-sm-8"><UserStatusBadge :status="user.status" /></dd>
              <dt class="col-sm-4">{{ $t('adminUsers.columns.createdAt') }}</dt>
              <dd class="col-sm-8 mb-0">{{ formatDateTime(user.createdAt) }}</dd>
            </dl>
          </fieldset>

          <fieldset class="border rounded bg-body p-3 mb-4">
            <legend class="float-none w-auto px-2 fs-5">
              {{ $t('adminUser.participantLegend') }}
            </legend>
            <dl v-if="hasParticipant" class="row mb-0">
              <dt class="col-sm-4">{{ $t('adminUsers.columns.participantName') }}</dt>
              <dd class="col-sm-8">{{ user.participantName }}</dd>
              <template v-if="showProfileEmail">
                <dt class="col-sm-4">{{ $t('adminUsers.columns.email') }}</dt>
                <dd class="col-sm-8">
                  <a :href="`mailto:${user.profileEmail}`">{{ user.profileEmail }}</a>
                </dd>
              </template>
              <dt class="col-sm-4">{{ $t('adminUsers.columns.phone') }}</dt>
              <dd class="col-sm-8 mb-0">
                <a :href="`tel:${user.phone}`">{{ user.phone }}</a>
              </dd>
            </dl>
            <p v-else class="text-secondary mb-0">{{ $t('adminUser.noParticipant') }}</p>
          </fieldset>

          <fieldset class="border rounded bg-body p-3 mb-4">
            <legend class="float-none w-auto px-2 fs-5">
              {{ $t('adminUser.registrationsLegend') }}
            </legend>
            <p v-if="user.registrations.length === 0" class="text-secondary mb-0">
              {{ $t('adminUser.noRegistrations') }}
            </p>
            <div v-else class="table-responsive">
              <table class="table table-hover align-middle mb-0">
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
                    <td class="text-nowrap">
                      <RouterLink
                        :to="{
                          name: 'adminCourseRoute',
                          query: { courseId: registration.courseId },
                        }"
                      >
                        {{ formatDateRange(registration.startDate, registration.endDate) }}
                      </RouterLink>
                      <span v-if="registration.isPast" class="badge text-bg-light border ms-1">
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
                            returnTo: returnTo,
                          },
                        }"
                        :title="$t('adminUser.viewRegistration')"
                        :aria-label="$t('adminUser.viewRegistration')"
                        class="btn btn-sm btn-outline-secondary d-inline-flex"
                      >
                        <PhEye :size="20" />
                      </RouterLink>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </fieldset>

          <div class="d-flex flex-wrap align-items-center gap-2 mb-5">
            <p v-if="isOwnAccount" class="text-secondary mb-0">
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
