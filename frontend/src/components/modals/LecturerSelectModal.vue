<script>
import BaseModal from '@/components/modals/BaseModal.vue'

export default {
  name: 'LecturerSelectModal',
  components: { BaseModal },
  props: {
    isOpen: Boolean,
    lecturers: Array,
  },
  emits: ['event-lecturer-search', 'event-lecturer-selected', 'event-modal-closed'],
  data() {
    return {
      search: '',
    }
  },
}
</script>

<template>
  <BaseModal :is-open="isOpen" @event-modal-closed="$emit('event-modal-closed')">
    <template #title>Vali lektor</template>
    <template #body>
      <div class="input-group mb-3">
        <input
          v-model="search"
          @keyup.enter="$emit('event-lecturer-search', search)"
          type="text"
          class="form-control"
          placeholder="Otsi nime järgi"
        />
        <button
          @click="$emit('event-lecturer-search', search)"
          class="btn btn-outline-secondary"
          type="button"
        >
          Otsi
        </button>
      </div>
      <div class="list-group">
        <button
          v-for="lecturer in lecturers"
          :key="lecturer.lecturerId"
          @click="$emit('event-lecturer-selected', lecturer)"
          class="list-group-item list-group-item-action"
          type="button"
        >
          {{ lecturer.lecturerName }}
        </button>
      </div>
      <p v-if="lecturers.length === 0" class="text-muted mb-0">Ühtegi lektorit ei leitud.</p>
    </template>
    <template #buttons>
      <button
        type="button"
        class="btn btn-outline-danger"
        @click="$emit('event-lecturer-selected', null)"
      >
        Lektor puudub
      </button>
    </template>
  </BaseModal>
</template>
