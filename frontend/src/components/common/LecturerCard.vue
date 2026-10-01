<script>
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import LecturerService from '@/api-services/LecturerService.js'
import LecturerAvatar from '@/components/common/LecturerAvatar.vue'

// Koolitaja kaart (pilt, nimi, ametinimetus, lühikirjeldus); kogu kaart on link /lecturer vaatesse
// (returnTo = praegune rada, et sealt saaks tagasi tulla). Laeb andmed ise lecturerId järgi ja
// uuesti keele või lecturerId muutumisel. Kaart on lisainfo: vea korral (nt 404 kustutatud
// koolitaja) seda ei kuvata ega suunata veavaatele — vaade saab teada sündmusega
// event-lecturer-not-found.
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
  <RouterLink
    v-if="lecturerSummary"
    :to="{
      name: 'lecturerRoute',
      query: { lecturerId: lecturerSummary.lecturerId, returnTo: $route.fullPath },
    }"
    :aria-label="$t('lecturerCard.viewProfile', { name: lecturerSummary.fullName })"
    class="d-flex gap-3 align-items-start rounded p-2 text-decoration-none text-body lecturer-card-link"
  >
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
  </RouterLink>
</template>

<style scoped>
/* Kogu kaart on link koolitaja detailvaatesse */
.lecturer-card-link {
  transition: box-shadow 0.15s ease;
}

.lecturer-card-link:hover,
.lecturer-card-link:focus-visible {
  box-shadow: var(--bs-box-shadow);
}
</style>
