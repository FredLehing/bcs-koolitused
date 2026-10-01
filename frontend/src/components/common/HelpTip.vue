<script>
import { PhQuestion } from '@phosphor-icons/vue'

// Abiteksti ikoon: tekst avaneb hiire all, fookusega ja klikiga (telefonis). Asendab Bootstrapi tooltip'i.
export default {
  name: 'HelpTip',
  components: { PhQuestion },
  props: {
    text: String,
  },
  data() {
    return {
      isOpen: false,
    }
  },
}
</script>

<template>
  <span class="group relative inline-flex">
    <button
      @click="isOpen = !isOpen"
      @blur="isOpen = false"
      :aria-label="text"
      :aria-expanded="isOpen"
      class="flex size-11 cursor-pointer items-center justify-center rounded-lg text-muted hover:bg-brand-50 hover:text-brand-700"
      type="button"
    >
      <PhQuestion :size="22" />
    </button>
    <span
      :class="isOpen ? 'block' : 'hidden group-hover:block'"
      class="absolute top-full right-0 z-20 mt-2 w-72 rounded-xl bg-navy p-3 text-sm text-white shadow-xl"
      role="tooltip"
    >
      {{ text }}
    </span>
  </span>
</template>
