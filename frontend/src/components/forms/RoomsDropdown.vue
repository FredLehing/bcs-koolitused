<script>
// Ruumi valik; esimene valik "Ruum puudub" = null
export default {
  name: 'RoomsDropdown',
  props: {
    roomId: {
      type: Number,
      default: null,
    },
    rooms: Array,
  },
  emits: ['event-new-room-selected'],
  methods: {
    handleChange(value) {
      this.$emit('event-new-room-selected', value === '' ? null : Number(value))
    },
  },
}
</script>

<template>
  <select :value="roomId ?? ''" @change="handleChange($event.target.value)" class="form-select">
    <option value="">{{ $t('courseForm.noRoom') }}</option>
    <option v-for="room in rooms" :key="room.roomId" :value="room.roomId">
      {{ room.roomName }}
    </option>
  </select>
</template>
