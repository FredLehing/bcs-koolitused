<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import EnquiryService from '@/api-services/EnquiryService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import FormatService from '@/services/FormatService.js'
import AlertSuccess from '@/components/common/AlertSuccess.vue'
import EnquiryStatusBadge from '@/components/common/EnquiryStatusBadge.vue'

// Üks huvilise päring: andmed, kontakt ja staatuse muutmine (uus ↔ käsitletud)
export default {
  name: 'AdminEnquiryView',
  components: { AlertSuccess, EnquiryStatusBadge },
  data() {
    return {
      successMessage: '',
      isSending: false,
      enquiryId: 0,
      enquiry: null,
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    isNew() {
      return this.enquiry?.status === 'U'
    },
  },
  watch: {
    // Keele vahetus → koolituse ja vormi nimi uues keeles
    contentLang() {
      this.getAdminEnquiry()
    },
  },
  methods: {
    loadView() {
      this.enquiryId = Number(this.$route.query.enquiryId ?? 0)
      if (this.enquiryId === 0) {
        NavigationService.navigateToErrorView()
        return
      }
      this.getAdminEnquiry()
    },

    getAdminEnquiry() {
      EnquiryService.sendGetAdminEnquiryRequest(this.enquiryId, this.contentLang)
        .then((response) => (this.enquiry = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    changeStatus() {
      const isHandle = this.isNew
      const request = isHandle
        ? EnquiryService.sendPutEnquiryHandleRequest(this.enquiryId)
        : EnquiryService.sendPutEnquiryReopenRequest(this.enquiryId)
      this.isSending = true
      request
        .then(() => this.handleChangeStatusResponse(isHandle))
        .catch(() => NavigationService.navigateToErrorView())
        .finally(() => (this.isSending = false))
    },

    handleChangeStatusResponse(isHandle) {
      this.successMessage = isHandle
        ? this.$t('adminEnquiry.messages.handled')
        : this.$t('adminEnquiry.messages.reopened')
      this.getAdminEnquiry()
    },

    navigateToAdminEnquiriesView() {
      NavigationService.navigateToAdminEnquiriesView()
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
    <div class="row justify-content-center">
      <div class="col-lg-8">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-3">
          <h1 class="mb-0">{{ $t('adminEnquiry.title') }}</h1>
          <button
            @click="navigateToAdminEnquiriesView"
            class="btn btn-outline-secondary"
            type="button"
          >
            {{ $t('navbar.manageEnquiries') }}
          </button>
        </div>

        <AlertSuccess :success-message="successMessage" />

        <template v-if="enquiry">
          <fieldset class="border rounded bg-body p-3 mb-4 text-start">
            <legend class="float-none w-auto px-2 fs-5">
              {{ $t('adminEnquiry.enquiryLegend') }}
            </legend>
            <dl class="row mb-0">
              <dt class="col-sm-4">{{ $t('adminEnquiry.createdAt') }}</dt>
              <dd class="col-sm-8">{{ formatDateTime(enquiry.createdAt) }}</dd>

              <dt class="col-sm-4">{{ $t('adminEnquiry.status') }}</dt>
              <dd class="col-sm-8"><EnquiryStatusBadge :status="enquiry.status" /></dd>

              <dt class="col-sm-4">{{ $t('adminEnquiry.training') }}</dt>
              <dd class="col-sm-8">
                <RouterLink
                  :to="{
                    name: 'trainingRoute',
                    query: {
                      trainingId: enquiry.trainingId,
                      trainingTranslationId: enquiry.trainingTranslationId,
                    },
                  }"
                >
                  {{ enquiry.trainingTitle }}
                </RouterLink>
              </dd>

              <dt class="col-sm-4">{{ $t('adminEnquiry.course') }}</dt>
              <dd class="col-sm-8">
                <template v-if="enquiry.courseStartDate">
                  {{ formatDateRange(enquiry.courseStartDate, enquiry.courseEndDate) }}
                </template>
                <span v-else class="text-secondary">{{ $t('adminEnquiry.noCourse') }}</span>
              </dd>

              <dt class="col-sm-4">{{ $t('adminEnquiry.option') }}</dt>
              <dd class="col-sm-8">{{ enquiry.optionName }}</dd>

              <dt class="col-sm-4">{{ $t('adminEnquiry.companyName') }}</dt>
              <dd class="col-sm-8">{{ enquiry.companyName ?? '—' }}</dd>

              <dt class="col-sm-4">{{ $t('adminEnquiry.message') }}</dt>
              <dd class="col-sm-8 mb-0 message">{{ enquiry.message }}</dd>
            </dl>
          </fieldset>

          <fieldset class="border rounded bg-body p-3 mb-4 text-start">
            <legend class="float-none w-auto px-2 fs-5">
              {{ $t('adminEnquiry.contactLegend') }}
            </legend>
            <dl class="row mb-0">
              <dt class="col-sm-4">{{ $t('adminEnquiry.fullName') }}</dt>
              <dd class="col-sm-8">{{ enquiry.fullName }}</dd>

              <dt class="col-sm-4">{{ $t('adminEnquiry.email') }}</dt>
              <dd class="col-sm-8">
                <a :href="`mailto:${enquiry.email}`">{{ enquiry.email }}</a>
              </dd>

              <dt class="col-sm-4">{{ $t('adminEnquiry.phone') }}</dt>
              <dd class="col-sm-8 mb-0">
                <a :href="`tel:${enquiry.phone}`">{{ enquiry.phone }}</a>
              </dd>
            </dl>
          </fieldset>

          <div class="mb-5">
            <button
              @click="changeStatus"
              :disabled="isSending"
              :class="isNew ? 'btn-success' : 'btn-outline-secondary'"
              class="btn"
              type="button"
            >
              {{ isNew ? $t('adminEnquiry.markHandled') : $t('adminEnquiry.markNew') }}
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Sõnumi reavahetused säilivad */
.message {
  white-space: pre-wrap;
}
</style>
