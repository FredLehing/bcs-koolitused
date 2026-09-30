<script>
import { PhArrowDown, PhArrowUp, PhPlus, PhX } from '@phosphor-icons/vue'
import LecturerService from '@/api-services/LecturerService.js'
import NavigationService from '@/services/NavigationService.js'
import LecturerSelectModal from '@/components/modals/LecturerSelectModal.vue'

// Valitud koolitajate nimekiri (järjekord = sort_order): × eemaldab, ↑ ↓ muudab järjekorda,
// "+ Lisa koolitaja" avab "Vali koolitaja" modali (juba valitud koolitajaid ei pakuta).
// lecturers = [{ lecturerId, lecturerName }]; iga muudatus → event-lecturers-changed uue listiga.
export default {
  name: 'LecturersPicker',
  components: { PhArrowDown, PhArrowUp, PhPlus, PhX, LecturerSelectModal },
  props: {
    lecturers: Array,
    isDisabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-lecturers-changed'],
  data() {
    return {
      isModalOpen: false,
      foundLecturers: [],
    }
  },
  computed: {
    selectedLecturerIds() {
      return this.lecturers.map((lecturer) => lecturer.lecturerId)
    },
  },
  methods: {
    openModal() {
      this.isModalOpen = true
      this.searchLecturers('')
    },

    searchLecturers(search) {
      LecturerService.sendGetLecturersRequest(search)
        .then((response) => (this.foundLecturers = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    addLecturer(lecturer) {
      this.isModalOpen = false
      this.$emit('event-lecturers-changed', [
        ...this.lecturers,
        { lecturerId: lecturer.lecturerId, lecturerName: lecturer.lecturerName },
      ])
    },

    removeLecturer(index) {
      this.$emit(
        'event-lecturers-changed',
        this.lecturers.filter((lecturer, lecturerIndex) => lecturerIndex !== index),
      )
    },

    // direction -1 = üles, 1 = alla
    moveLecturer(index, direction) {
      const reorderedLecturers = [...this.lecturers]
      const [movedLecturer] = reorderedLecturers.splice(index, 1)
      reorderedLecturers.splice(index + direction, 0, movedLecturer)
      this.$emit('event-lecturers-changed', reorderedLecturers)
    },
  },
}
</script>

<template>
  <div>
    <ol v-if="lecturers.length > 0" class="list-group list-group-numbered mb-2">
      <li
        v-for="(lecturer, index) in lecturers"
        :key="lecturer.lecturerId"
        class="list-group-item d-flex align-items-center gap-2"
      >
        <span class="me-auto">{{ lecturer.lecturerName }}</span>
        <template v-if="!isDisabled">
          <button
            @click="moveLecturer(index, -1)"
            :disabled="index === 0"
            :title="$t('lecturersPicker.moveUp')"
            :aria-label="$t('lecturersPicker.moveUp')"
            class="btn btn-sm btn-outline-secondary d-inline-flex"
            type="button"
          >
            <PhArrowUp :size="16" />
          </button>
          <button
            @click="moveLecturer(index, 1)"
            :disabled="index === lecturers.length - 1"
            :title="$t('lecturersPicker.moveDown')"
            :aria-label="$t('lecturersPicker.moveDown')"
            class="btn btn-sm btn-outline-secondary d-inline-flex"
            type="button"
          >
            <PhArrowDown :size="16" />
          </button>
          <button
            @click="removeLecturer(index)"
            :title="$t('lecturersPicker.remove')"
            :aria-label="$t('lecturersPicker.remove')"
            class="btn btn-sm btn-outline-danger d-inline-flex"
            type="button"
          >
            <PhX :size="16" />
          </button>
        </template>
      </li>
    </ol>
    <p v-else class="text-secondary mb-2">{{ $t('lecturersPicker.empty') }}</p>
    <button
      v-if="!isDisabled"
      @click="openModal"
      class="btn btn-sm btn-outline-primary d-inline-flex align-items-center gap-1"
      type="button"
    >
      <PhPlus :size="16" />
      {{ $t('lecturersPicker.add') }}
    </button>

    <LecturerSelectModal
      :is-open="isModalOpen"
      :lecturers="foundLecturers"
      :excluded-lecturer-ids="selectedLecturerIds"
      @event-lecturer-search="searchLecturers"
      @event-lecturer-selected="addLecturer"
      @event-modal-closed="isModalOpen = false"
    />
  </div>
</template>
