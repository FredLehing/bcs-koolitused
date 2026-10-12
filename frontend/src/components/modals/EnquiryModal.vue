<script>
import EnquiryService from '@/api-services/EnquiryService.js'
import FormatService from '@/services/FormatService.js'
import NavigationService from '@/services/NavigationService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'
import BaseModal from '@/components/modals/BaseModal.vue'

const MESSAGE_MAX_LENGTH = 255
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function createEmptyEnquiry() {
  return {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    companyName: '',
    message: '',
  }
}

// Modal "Küsi lisainfot": kontrollib välju, saadab POST /api/enquiry ise ja teatab vaatele event-enquiry-sent.
// courseId on valikuline (üldine päring koolituse kohta).
export default {
  name: 'EnquiryModal',
  components: { AlertDanger, BaseModal },
  props: {
    isOpen: Boolean,
    trainingId: Number,
    courseId: {
      type: Number,
      default: null,
    },
    title: String,
    startDate: {
      type: String,
      default: '',
    },
    endDate: {
      type: String,
      default: '',
    },
  },
  emits: ['event-enquiry-sent', 'event-modal-closed'],
  data() {
    return {
      enquiry: createEmptyEnquiry(),
      errorMessage: '',
      isSending: false,
    }
  },
  computed: {
    messageMaxLength() {
      return MESSAGE_MAX_LENGTH
    },

    contextText() {
      const dates = FormatService.formatDateRange(this.startDate, this.endDate)
      return dates ? `${this.title} · ${dates}` : this.title
    },
  },
  watch: {
    // Iga avamine algab tühja vormiga
    isOpen(isOpen) {
      if (isOpen) {
        this.enquiry = createEmptyEnquiry()
        this.errorMessage = ''
      }
    },
  },
  methods: {
    sendEnquiry() {
      this.errorMessage = ''
      this.checkEnquiryForErrors()
      if (this.errorMessage !== '') {
        return
      }
      this.isSending = true
      EnquiryService.sendPostEnquiryRequest({
        trainingId: this.trainingId,
        courseId: this.courseId,
        firstName: this.enquiry.firstName.trim(),
        lastName: this.enquiry.lastName.trim(),
        email: this.enquiry.email.trim(),
        phone: this.enquiry.phone.trim(),
        companyName: this.enquiry.companyName.trim(),
        message: this.enquiry.message.trim(),
      })
        .then(() => this.$emit('event-enquiry-sent'))
        .catch((error) => this.handleSendEnquiryError(error))
        .finally(() => (this.isSending = false))
    },

    // 400 (valideerimine) ja 404 (toimumiskord vahepeal suletud) → teade modalis
    handleSendEnquiryError(error) {
      const statusCode = error.response?.status
      if (statusCode === 400 || statusCode === 404) {
        this.errorMessage = error.response.data?.message ?? this.$t('enquiryModal.sendFailed')
      } else {
        NavigationService.navigateToErrorView()
        this.errorMessage = this.$t('enquiryModal.sendFailed')
      }
    },

    checkEnquiryForErrors() {
      const requiredValues = [
        this.enquiry.firstName,
        this.enquiry.lastName,
        this.enquiry.email,
        this.enquiry.phone,
        this.enquiry.message,
      ]
      if (requiredValues.some((value) => value.trim() === '')) {
        this.errorMessage = this.$t('enquiryModal.validation.fillRequired')
      } else if (!EMAIL_PATTERN.test(this.enquiry.email.trim())) {
        this.errorMessage = this.$t('enquiryModal.validation.invalidEmail')
      }
    },

    close() {
      this.$emit('event-modal-closed')
    },

    handleKeydown(event) {
      if (this.isOpen && event.key === 'Escape') {
        this.close()
      }
    },
  },
  mounted() {
    window.addEventListener('keydown', this.handleKeydown)
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.handleKeydown)
  },
}
</script>

<template>
  <BaseModal :is-open="isOpen" @event-modal-closed="close">
    <template #title>
      {{ $t('enquiryModal.title') }}
      <div class="mt-0.5 font-sans text-sm font-normal text-muted">{{ contextText }}</div>
    </template>
    <template #body>
      <div class="grid gap-3 sm:grid-cols-2">
        <div>
          <label class="form-label" for="enquiry-first-name">
            {{ $t('enquiryModal.firstName') }} *
          </label>
          <input
            v-model="enquiry.firstName"
            id="enquiry-first-name"
            class="form-control"
            type="text"
            maxlength="255"
            autocomplete="given-name"
          />
        </div>
        <div>
          <label class="form-label" for="enquiry-last-name">
            {{ $t('enquiryModal.lastName') }} *
          </label>
          <input
            v-model="enquiry.lastName"
            id="enquiry-last-name"
            class="form-control"
            type="text"
            maxlength="255"
            autocomplete="family-name"
          />
        </div>
        <div>
          <label class="form-label" for="enquiry-email">{{ $t('enquiryModal.email') }} *</label>
          <input
            v-model="enquiry.email"
            id="enquiry-email"
            class="form-control"
            type="email"
            maxlength="255"
            autocomplete="email"
          />
        </div>
        <div>
          <label class="form-label" for="enquiry-phone">{{ $t('enquiryModal.phone') }} *</label>
          <input
            v-model="enquiry.phone"
            id="enquiry-phone"
            class="form-control"
            type="tel"
            maxlength="20"
            autocomplete="tel"
          />
        </div>
        <div class="sm:col-span-2">
          <label class="form-label" for="enquiry-company">{{ $t('enquiryModal.company') }}</label>
          <input
            v-model="enquiry.companyName"
            id="enquiry-company"
            class="form-control"
            type="text"
            maxlength="255"
            autocomplete="organization"
          />
        </div>
        <div class="sm:col-span-2">
          <label class="form-label" for="enquiry-message">{{ $t('enquiryModal.message') }} *</label>
          <textarea
            v-model="enquiry.message"
            id="enquiry-message"
            class="form-control"
            rows="4"
            :maxlength="messageMaxLength"
          ></textarea>
          <div class="form-text text-right">
            {{ enquiry.message.length }} / {{ messageMaxLength }}
          </div>
        </div>
      </div>
      <p class="mt-3 mb-3 text-sm text-muted">{{ $t('enquiryModal.privacy') }}</p>
      <AlertDanger :error-message="errorMessage" />
    </template>
    <template #buttons>
      <button @click="sendEnquiry" :disabled="isSending" class="btn btn-primary" type="button">
        {{ $t('enquiryModal.send') }}
      </button>
    </template>
  </BaseModal>
</template>
