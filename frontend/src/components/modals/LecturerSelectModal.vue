<script>
import BaseModal from '@/components/modals/BaseModal.vue'

export default {
  name: 'LecturerSelectModal',
  components: { BaseModal },
  props: {
    isOpen: Boolean,
    lecturers: Array,
    // Juba valitud koolitajad — neid nimekirjas ei pakuta
    excludedLecturerIds: {
      type: Array,
      default: () => [],
    },
  },
  emits: ['event-lecturer-search', 'event-lecturer-selected', 'event-modal-closed'],
  data() {
    return {
      search: '',
    }
  },
  computed: {
    selectableLecturers() {
      return this.lecturers.filter(
        (lecturer) => !this.excludedLecturerIds.includes(lecturer.lecturerId),
      )
    },
  },
}
</script>

<template>
  <BaseModal :is-open="isOpen" @event-modal-closed="$emit('event-modal-closed')">
    <template #title>{{ $t('trainingForm.lecturerModal.title') }}</template>
    <template #body>
      <div class="mb-4 flex">
        <input
          v-model="search"
          @keyup.enter="$emit('event-lecturer-search', search)"
          type="text"
          class="form-control rounded-r-none"
          :aria-label="$t('trainingForm.lecturerModal.searchPlaceholder')"
          :placeholder="$t('trainingForm.lecturerModal.searchPlaceholder')"
        />
        <button
          @click="$emit('event-lecturer-search', search)"
          class="btn btn-outline-secondary -ml-px rounded-l-none border-brand-200"
          type="button"
        >
          {{ $t('trainingForm.lecturerModal.search') }}
        </button>
      </div>
      <div
        v-if="selectableLecturers.length > 0"
        class="divide-y divide-line overflow-hidden rounded-xl border border-line"
      >
        <button
          v-for="lecturer in selectableLecturers"
          :key="lecturer.lecturerId"
          @click="$emit('event-lecturer-selected', lecturer)"
          class="block min-h-11 w-full cursor-pointer px-4 py-2.5 text-left hover:bg-brand-50 hover:text-brand-700"
          type="button"
        >
          {{ lecturer.lecturerName }}
        </button>
      </div>
      <p v-else class="text-muted">
        {{ $t('trainingForm.lecturerModal.notFound') }}
      </p>
    </template>
  </BaseModal>
</template>
