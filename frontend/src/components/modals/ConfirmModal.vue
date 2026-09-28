<script>
import BaseModal from '@/components/modals/BaseModal.vue'

export default {
  name: 'ConfirmModal',
  components: { BaseModal },
  props: {
    isOpen: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
  },
  emits: ['event-confirmed', 'event-modal-closed'],
}
</script>

<template>
  <BaseModal :is-open="isOpen" @event-modal-closed="$emit('event-modal-closed')">
    <template #title>{{ title }}</template>
    <template #body>{{ message }}</template>
    <template #buttons>
      <button type="button" class="btn btn-primary" @click="$emit('event-confirmed')">
        {{ confirmLabel }}
      </button>
    </template>
  </BaseModal>
</template>
