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
      <div class="input-group mb-3">
        <input
          v-model="search"
          @keyup.enter="$emit('event-lecturer-search', search)"
          type="text"
          class="form-control"
          :placeholder="$t('trainingForm.lecturerModal.searchPlaceholder')"
        />
        <button
          @click="$emit('event-lecturer-search', search)"
          class="btn btn-outline-secondary"
          type="button"
        >
          {{ $t('trainingForm.lecturerModal.search') }}
        </button>
      </div>
      <div class="list-group">
        <button
          v-for="lecturer in selectableLecturers"
          :key="lecturer.lecturerId"
          @click="$emit('event-lecturer-selected', lecturer)"
          class="list-group-item list-group-item-action"
          type="button"
        >
          {{ lecturer.lecturerName }}
        </button>
      </div>
      <p v-if="selectableLecturers.length === 0" class="text-muted mb-0">
        {{ $t('trainingForm.lecturerModal.notFound') }}
      </p>
    </template>
  </BaseModal>
</template>
