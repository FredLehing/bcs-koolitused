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
    <ol
      v-if="lecturers.length > 0"
      class="mb-3 divide-y divide-line overflow-hidden rounded-xl border border-line bg-white"
    >
      <li
        v-for="(lecturer, index) in lecturers"
        :key="lecturer.lecturerId"
        class="flex min-h-13 items-center gap-2 px-3 py-2"
      >
        <span
          class="flex size-7 shrink-0 items-center justify-center rounded-full bg-brand-100 text-sm font-bold text-brand-700 tabular-nums"
        >
          {{ index + 1 }}
        </span>
        <span class="mr-auto font-semibold">{{ lecturer.lecturerName }}</span>
        <template v-if="!isDisabled">
          <button
            @click="moveLecturer(index, -1)"
            :disabled="index === 0"
            :title="$t('lecturersPicker.moveUp')"
            :aria-label="$t('lecturersPicker.moveUp')"
            class="btn btn-outline-secondary btn-sm btn-icon"
            type="button"
          >
            <PhArrowUp :size="16" />
          </button>
          <button
            @click="moveLecturer(index, 1)"
            :disabled="index === lecturers.length - 1"
            :title="$t('lecturersPicker.moveDown')"
            :aria-label="$t('lecturersPicker.moveDown')"
            class="btn btn-outline-secondary btn-sm btn-icon"
            type="button"
          >
            <PhArrowDown :size="16" />
          </button>
          <button
            @click="removeLecturer(index)"
            :title="$t('lecturersPicker.remove')"
            :aria-label="$t('lecturersPicker.remove')"
            class="btn btn-outline-danger btn-sm btn-icon"
            type="button"
          >
            <PhX :size="16" />
          </button>
        </template>
      </li>
    </ol>
    <p v-else class="mb-3 text-muted">{{ $t('lecturersPicker.empty') }}</p>
    <button
      v-if="!isDisabled"
      @click="openModal"
      class="btn btn-outline-primary btn-sm"
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
