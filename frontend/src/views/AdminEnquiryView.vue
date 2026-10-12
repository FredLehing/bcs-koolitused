<script>
import BackLink from '@/components/common/BackLink.vue'
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
  components: { BackLink, AlertSuccess, EnquiryStatusBadge },
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
    // Keele vahetus → koolituse nimi uues keeles
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
  <div class="mx-auto w-full max-w-7xl px-6 py-8">
    <BackLink :fallback="{ name: 'adminEnquiriesRoute' }" />
    <div class="flex justify-center">
      <div class="w-full max-w-4xl">
        <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
          <h1 class="text-3xl font-extrabold tracking-tight">{{ $t('adminEnquiry.title') }}</h1>
          <button
            @click="navigateToAdminEnquiriesView"
            class="btn btn-outline-secondary"
            type="button"
          >
            {{ $t('navbar.manageEnquiries') }}
          </button>
        </div>

        <AlertSuccess :success-message="successMessage" class="mb-4" />

        <template v-if="enquiry">
          <section class="mb-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">
              {{ $t('adminEnquiry.enquiryLegend') }}
            </h2>
            <dl class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-[12rem_1fr]">
              <dt class="text-muted">{{ $t('adminEnquiry.createdAt') }}</dt>
              <dd>{{ formatDateTime(enquiry.createdAt) }}</dd>

              <dt class="text-muted">{{ $t('adminEnquiry.status') }}</dt>
              <dd><EnquiryStatusBadge :status="enquiry.status" /></dd>

              <dt class="text-muted">{{ $t('adminEnquiry.training') }}</dt>
              <dd>
                <RouterLink
                  :to="{
                    name: 'trainingRoute',
                    query: {
                      returnTo: $route.fullPath,
                      trainingId: enquiry.trainingId,
                      trainingTranslationId: enquiry.trainingTranslationId,
                    },
                  }"
                >
                  {{ enquiry.trainingTitle }}
                </RouterLink>
              </dd>

              <dt class="text-muted">{{ $t('adminEnquiry.course') }}</dt>
              <dd>
                <template v-if="enquiry.courseStartDate">
                  {{ formatDateRange(enquiry.courseStartDate, enquiry.courseEndDate) }}
                </template>
                <span v-else class="text-muted">{{ $t('adminEnquiry.noCourse') }}</span>
              </dd>

              <dt class="text-muted">{{ $t('adminEnquiry.companyName') }}</dt>
              <dd>{{ enquiry.companyName ?? '—' }}</dd>

              <dt class="text-muted">{{ $t('adminEnquiry.message') }}</dt>
              <dd class="whitespace-pre-wrap">{{ enquiry.message }}</dd>
            </dl>
          </section>

          <section class="mb-6 rounded-2xl border border-line bg-white p-5 sm:p-6">
            <h2 class="mb-4 text-lg font-bold">
              {{ $t('adminEnquiry.contactLegend') }}
            </h2>
            <dl class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-[12rem_1fr]">
              <dt class="text-muted">{{ $t('adminEnquiry.fullName') }}</dt>
              <dd>{{ enquiry.fullName }}</dd>

              <dt class="text-muted">{{ $t('adminEnquiry.email') }}</dt>
              <dd>
                <a :href="`mailto:${enquiry.email}`">{{ enquiry.email }}</a>
              </dd>

              <dt class="text-muted">{{ $t('adminEnquiry.phone') }}</dt>
              <dd>
                <a :href="`tel:${enquiry.phone}`">{{ enquiry.phone }}</a>
              </dd>
            </dl>
          </section>

          <div class="mb-6">
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
