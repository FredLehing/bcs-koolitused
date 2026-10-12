<script>
import BackLink from '@/components/common/BackLink.vue'
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import CourseParticipantService from '@/api-services/CourseParticipantService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import FormatService from '@/services/FormatService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'
import AlertSuccess from '@/components/common/AlertSuccess.vue'
import CourseStatusBadge from '@/components/common/CourseStatusBadge.vue'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

// Üks registreerumine (admin): osaleja ja toimumiskord ainult lugemiseks, registreerumise
// staatus, tasumine, sülearvuti vajadus ja admini märkmed muudetavad
export default {
  name: 'AdminRegistrationView',
  components: { BackLink, AlertDanger, AlertSuccess, CourseStatusBadge, ConfirmModal },
  data() {
    return {
      successMessage: '',
      errorMessage: '',
      isSending: false,
      isStatusConfirmModalOpen: false,
      courseParticipantId: 0,
      registration: null,
      // Vormi väljad (PUT body); "Tühista" taastab need laaditud registreerumisest
      registrationForm: {
        status: 'R',
        hasPaid: false,
        requiresLaptop: false,
        adminNotes: '',
      },
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    isStatusChanged() {
      return this.registrationForm.status !== this.registration.status
    },

    // Konto e-post kuvatakse ainult siis, kui see erineb registreerumise e-postist
    showAccountEmail() {
      return (
        this.registration.accountEmail && this.registration.accountEmail !== this.registration.email
      )
    },

    isCancelling() {
      return this.registrationForm.status === 'C'
    },

    statusConfirmTitle() {
      return this.isCancelling
        ? this.$t('adminRegistration.confirmCancelTitle')
        : this.$t('adminRegistration.confirmRestoreTitle')
    },

    // Täis toimumiskorrale taastamine on lubatud, kuid admin saab sellest teada
    statusConfirmMessage() {
      const params = {
        participantName: this.registration.participantName,
        trainingTitle: this.registration.trainingTitle,
        courseStartDate: FormatService.formatLocalDate(this.registration.courseStartDate),
      }
      if (this.isCancelling) {
        return this.$t('adminRegistration.confirmCancelMessage', params)
      }
      const message = this.$t('adminRegistration.confirmRestoreMessage', params)
      return this.registration.courseStatus === 'F'
        ? `${message} ${this.$t('adminRegistration.courseFullNote')}`
        : message
    },
  },
  watch: {
    '$route.query'() {
      this.loadView()
    },

    // Keele vahetus → koolituse nimi uues keeles; vormi salvestamata muudatused jäävad alles
    contentLang() {
      this.getAdminRegistration(true)
    },
  },
  methods: {
    loadView() {
      this.courseParticipantId = Number(this.$route.query.courseParticipantId ?? 0)
      if (this.courseParticipantId === 0) {
        NavigationService.navigateToErrorView()
        return
      }
      this.getAdminRegistration()
    },

    // Olematu registreerumine (404) → üldine veavaade. keepForm: vormi väärtusi ei taastata
    getAdminRegistration(keepForm = false) {
      CourseParticipantService.sendGetAdminRegistrationRequest(
        this.courseParticipantId,
        this.contentLang,
      )
        .then((response) => this.handleGetAdminRegistrationResponse(response.data, keepForm))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetAdminRegistrationResponse(registration, keepForm) {
      this.registration = registration
      if (!keepForm) {
        this.resetForm()
      }
    },

    resetForm() {
      this.registrationForm = {
        status: this.registration.status,
        hasPaid: this.registration.hasPaid,
        requiresLaptop: this.registration.requiresLaptop,
        adminNotes: this.registration.adminNotes ?? '',
      }
    },

    cancelChanges() {
      this.errorMessage = ''
      this.resetForm()
    },

    // ---------- "Salvesta" ----------

    // Staatuse muutmine (loobunuks / taastamine) küsitakse enne üle
    handleSaveClick() {
      if (this.isStatusChanged) {
        this.isStatusConfirmModalOpen = true
      } else {
        this.saveRegistration()
      }
    },

    handleStatusConfirmed() {
      this.isStatusConfirmModalOpen = false
      this.saveRegistration()
    },

    saveRegistration() {
      this.successMessage = ''
      this.errorMessage = ''
      this.isSending = true
      CourseParticipantService.sendPutAdminRegistrationRequest(
        this.courseParticipantId,
        this.registrationForm,
      )
        .then(() => this.handleSaveRegistrationResponse())
        .catch((error) => this.handleSaveRegistrationError(error))
        .finally(() => (this.isSending = false))
    },

    handleSaveRegistrationResponse() {
      this.successMessage = this.$t('adminRegistration.messages.saved')
      this.getAdminRegistration()
    },

    // 400 → backendi teade vormis; muu viga → üldine teade vormis
    handleSaveRegistrationError(error) {
      this.errorMessage =
        error.response?.data?.message ?? this.$t('adminRegistration.messages.saveFailed')
    },

    formatDateTime(instant) {
      return FormatService.formatDateTime(instant)
    },

    formatDateRange(startDate, endDate) {
      return FormatService.formatDateRange(startDate, endDate)
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.loadView()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
  },
}
</script>

<template>
  <div class="mx-auto w-full max-w-7xl px-6 py-8">
    <BackLink :fallback="{ name: 'adminRegistrationsRoute' }" />
    <div class="flex justify-center">
      <div class="w-full max-w-4xl">
        <template v-if="registration">
          <h1 class="text-3xl font-extrabold tracking-tight">
            {{ $t('adminRegistration.title') }}
          </h1>
          <p class="mb-6 mt-1 text-lg text-muted">{{ registration.participantName }}</p>

          <AlertSuccess :success-message="successMessage" class="mb-4" />

          <section class="mb-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">
              {{ $t('adminRegistration.participantLegend') }}
            </h2>
            <dl class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-[12rem_1fr]">
              <dt class="text-muted">{{ $t('adminRegistration.participantName') }}</dt>
              <dd>{{ registration.participantName }}</dd>

              <dt class="text-muted">{{ $t('adminRegistration.email') }}</dt>
              <dd>
                <a :href="`mailto:${registration.email}`">{{ registration.email }}</a>
              </dd>

              <dt class="text-muted">{{ $t('adminRegistration.phone') }}</dt>
              <dd>
                <a :href="`tel:${registration.phone}`">{{ registration.phone }}</a>
              </dd>

              <template v-if="showAccountEmail">
                <dt class="text-muted">{{ $t('adminRegistration.accountEmail') }}</dt>
                <dd>{{ registration.accountEmail }}</dd>
              </template>
            </dl>
            <p class="form-text mt-3">{{ $t('adminRegistration.participantHint') }}</p>
          </section>

          <section class="mb-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">
              {{ $t('adminRegistration.courseLegend') }}
            </h2>
            <dl class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-[12rem_1fr]">
              <dt class="text-muted">{{ $t('adminRegistration.training') }}</dt>
              <dd>{{ registration.trainingTitle }}</dd>

              <dt class="text-muted">{{ $t('adminRegistration.courseDates') }}</dt>
              <dd>
                {{ formatDateRange(registration.courseStartDate, registration.courseEndDate) }}
              </dd>

              <dt class="text-muted">{{ $t('adminRegistration.courseStatus') }}</dt>
              <dd>
                <CourseStatusBadge
                  :status="registration.courseStatus"
                  :is-past="registration.isPast"
                />
              </dd>
            </dl>
            <RouterLink
              :to="{
                name: 'adminCourseRoute',
                query: { returnTo: $route.fullPath, courseId: registration.courseId },
              }"
            >
              {{ $t('adminRegistration.openCourse') }}
            </RouterLink>
          </section>

          <section class="mb-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">
              {{ $t('adminRegistration.registrationLegend') }}
            </h2>

            <div class="mb-4">
              <div class="form-label">{{ $t('adminRegistration.status') }}</div>
              <div class="flex flex-wrap gap-x-6 gap-y-2">
                <div class="form-check">
                  <input
                    v-model="registrationForm.status"
                    id="statusRegistered"
                    class="form-check-input"
                    type="radio"
                    value="R"
                  />
                  <label class="form-check-label" for="statusRegistered">
                    {{ $t('courseParticipantStatus.R') }}
                  </label>
                </div>
                <div class="form-check">
                  <input
                    v-model="registrationForm.status"
                    id="statusCancelled"
                    class="form-check-input"
                    type="radio"
                    value="C"
                  />
                  <label class="form-check-label" for="statusCancelled">
                    {{ $t('courseParticipantStatus.C') }}
                  </label>
                </div>
              </div>
            </div>

            <div class="form-check form-switch mb-2">
              <input
                v-model="registrationForm.hasPaid"
                id="hasPaid"
                class="form-check-input"
                type="checkbox"
                role="switch"
              />
              <label class="form-check-label" for="hasPaid">
                {{ $t('adminRegistration.hasPaid') }}
              </label>
            </div>
            <div class="form-check form-switch mb-4">
              <input
                v-model="registrationForm.requiresLaptop"
                id="requiresLaptop"
                class="form-check-input"
                type="checkbox"
                role="switch"
              />
              <label class="form-check-label" for="requiresLaptop">
                {{ $t('adminRegistration.requiresLaptop') }}
              </label>
            </div>

            <div class="mb-4">
              <div class="form-label">{{ $t('adminRegistration.notes') }}</div>
              <div class="whitespace-pre-wrap text-muted">{{ registration.notes || '—' }}</div>
            </div>

            <div class="mb-4">
              <label class="form-label" for="adminNotes">
                {{ $t('adminRegistration.adminNotes') }}
              </label>
              <textarea
                v-model="registrationForm.adminNotes"
                id="adminNotes"
                class="form-control"
                rows="3"
              ></textarea>
            </div>

            <dl
              class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-[12rem_1fr] mt-4 text-sm text-muted"
            >
              <dt class="text-muted">{{ $t('adminRegistration.registeredAt') }}</dt>
              <dd>{{ formatDateTime(registration.createdAt) }}</dd>

              <dt class="text-muted">{{ $t('adminRegistration.updatedAt') }}</dt>
              <dd>{{ formatDateTime(registration.updatedAt) }}</dd>
            </dl>
          </section>

          <AlertDanger :error-message="errorMessage" class="mb-4" />

          <div class="mb-6 flex gap-2">
            <button
              @click="handleSaveClick"
              :disabled="isSending"
              class="btn btn-primary"
              type="button"
            >
              {{ $t('adminRegistration.save') }}
            </button>
            <button
              @click="cancelChanges"
              :disabled="isSending"
              class="btn btn-outline-secondary"
              type="button"
            >
              {{ $t('adminRegistration.cancel') }}
            </button>
          </div>
        </template>
      </div>
    </div>

    <ConfirmModal
      :is-open="isStatusConfirmModalOpen"
      :title="statusConfirmTitle"
      :message="statusConfirmMessage"
      :confirm-label="$t('adminRegistration.confirm')"
      @event-confirmed="handleStatusConfirmed"
      @event-modal-closed="isStatusConfirmModalOpen = false"
    />
  </div>
</template>
