<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import LecturerService from '@/api-services/LecturerService.js'
import LecturerAvatar from '@/components/common/LecturerAvatar.vue'

// Koolitaja kaart (pilt, nimi, ametinimetus, lühikirjeldus). Laeb andmed ise lecturerId järgi ja uuesti
// keele või lecturerId muutumisel. Kaart on lisainfo: vea korral (nt 404 kustutatud koolitaja) seda
// ei kuvata ega suunata veavaatele — vaade saab teada sündmusega event-lecturer-not-found.
export default {
  name: 'LecturerCard',
  components: { LecturerAvatar },
  props: {
    lecturerId: {
      type: Number,
      default: null,
    },
  },
  emits: ['event-lecturer-not-found'],
  data() {
    return {
      lecturerSummary: null,
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),
  },
  watch: {
    lecturerId() {
      this.getLecturerSummary()
    },

    contentLang() {
      this.getLecturerSummary()
    },
  },
  methods: {
    getLecturerSummary() {
      if (this.lecturerId === null) {
        this.lecturerSummary = null
        return
      }
      LecturerService.sendGetLecturerSummaryRequest(this.lecturerId, this.contentLang)
        .then((response) => (this.lecturerSummary = response.data))
        .catch(() => this.handleGetLecturerSummaryError())
    },

    handleGetLecturerSummaryError() {
      this.lecturerSummary = null
      this.$emit('event-lecturer-not-found', this.lecturerId)
    },
  },
  beforeMount() {
    this.getLecturerSummary()
  },
}
</script>

<template>
  <div v-if="lecturerSummary" class="d-flex gap-3 align-items-start">
    <LecturerAvatar
      :lecturer-id="lecturerSummary.lecturerId"
      :photo-version="lecturerSummary.photoVersion"
      :alt="lecturerSummary.fullName"
      :size="56"
    />
    <div>
      <div class="fw-bold">{{ lecturerSummary.fullName }}</div>
      <div class="small text-secondary mb-1">{{ lecturerSummary.title }}</div>
      <div class="small">{{ lecturerSummary.shortDescription }}</div>
    </div>
  </div>
</template>
