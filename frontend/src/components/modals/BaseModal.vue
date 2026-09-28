<script>
export default {
  name: 'BaseModal',
  props: {
    isOpen: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-modal-closed'],
  methods: {
    close() {
      this.$emit('event-modal-closed')
    },
  },
}
</script>

<template>
  <div v-if="isOpen">
    <div class="modal d-block" tabindex="-1" @click.self="close">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <slot name="title"></slot>
            </h5>
            <button type="button" class="btn-close" @click="close" />
          </div>
          <div class="modal-body">
            <slot name="body"></slot>
          </div>
          <div class="modal-footer">
            <slot name="buttons"></slot>
            <button type="button" class="btn btn-secondary" @click="close">Sulge</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop show" />
  </div>
</template>
