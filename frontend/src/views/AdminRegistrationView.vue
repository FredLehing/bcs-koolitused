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
  <div class="container">
    <BackLink :fallback="{ name: 'adminRegistrationsRoute' }" />
    <div class="row justify-content-center">
      <div class="col-lg-8">
        <template v-if="registration">
          <h1 class="mb-1">{{ $t('adminRegistration.title') }}</h1>
          <p class="text-secondary fs-5 mb-3">{{ registration.participantName }}</p>

          <AlertSuccess :success-message="successMessage" />

          <fieldset class="border rounded bg-body p-3 mb-4 text-start">
            <legend class="float-none w-auto px-2 fs-5">
              {{ $t('adminRegistration.participantLegend') }}
            </legend>
            <dl class="row mb-0">
              <dt class="col-sm-4">{{ $t('adminRegistration.participantName') }}</dt>
              <dd class="col-sm-8">{{ registration.participantName }}</dd>

              <dt class="col-sm-4">{{ $t('adminRegistration.email') }}</dt>
              <dd class="col-sm-8">
                <a :href="`mailto:${registration.email}`">{{ registration.email }}</a>
              </dd>

              <dt class="col-sm-4">{{ $t('adminRegistration.phone') }}</dt>
              <dd class="col-sm-8" :class="{ 'mb-0': !showAccountEmail }">
                <a :href="`tel:${registration.phone}`">{{ registration.phone }}</a>
              </dd>

              <template v-if="showAccountEmail">
                <dt class="col-sm-4">{{ $t('adminRegistration.accountEmail') }}</dt>
                <dd class="col-sm-8 mb-0">{{ registration.accountEmail }}</dd>
              </template>
            </dl>
            <p class="form-text mb-0 mt-2">{{ $t('adminRegistration.participantHint') }}</p>
          </fieldset>

          <fieldset class="border rounded bg-body p-3 mb-4 text-start">
            <legend class="float-none w-auto px-2 fs-5">
              {{ $t('adminRegistration.courseLegend') }}
            </legend>
            <dl class="row">
              <dt class="col-sm-4">{{ $t('adminRegistration.training') }}</dt>
              <dd class="col-sm-8">{{ registration.trainingTitle }}</dd>

              <dt class="col-sm-4">{{ $t('adminRegistration.courseDates') }}</dt>
              <dd class="col-sm-8">
                {{ formatDateRange(registration.courseStartDate, registration.courseEndDate) }}
              </dd>

              <dt class="col-sm-4">{{ $t('adminRegistration.courseStatus') }}</dt>
              <dd class="col-sm-8">
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
          </fieldset>

          <fieldset class="border rounded bg-body p-3 mb-4 text-start">
            <legend class="float-none w-auto px-2 fs-5">
              {{ $t('adminRegistration.registrationLegend') }}
            </legend>

            <div class="mb-3">
              <div class="form-label">{{ $t('adminRegistration.status') }}</div>
              <div class="form-check form-check-inline">
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
              <div class="form-check form-check-inline">
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
            <div class="form-check form-switch mb-3">
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

            <div class="mb-3">
              <div class="form-label">{{ $t('adminRegistration.notes') }}</div>
              <div class="notes text-body-secondary">{{ registration.notes || '—' }}</div>
            </div>

            <div class="mb-3">
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

            <dl class="row small text-secondary mb-0">
              <dt class="col-sm-4 fw-normal">{{ $t('adminRegistration.registeredAt') }}</dt>
              <dd class="col-sm-8">{{ formatDateTime(registration.createdAt) }}</dd>

              <dt class="col-sm-4 fw-normal">{{ $t('adminRegistration.updatedAt') }}</dt>
              <dd class="col-sm-8 mb-0">{{ formatDateTime(registration.updatedAt) }}</dd>
            </dl>
          </fieldset>

          <AlertDanger :error-message="errorMessage" />

          <div class="d-flex gap-2 mb-5">
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

<style scoped>
/* Osaleja lisainfo reavahetused säilivad */
.notes {
  white-space: pre-wrap;
}
</style>
