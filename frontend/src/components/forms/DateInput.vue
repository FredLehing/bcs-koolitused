<script>
import { PhCalendarBlank } from '@phosphor-icons/vue'
import FormatService from '@/services/FormatService.js'

// Kuupäeva väli kujul pp/kk/aaaa. Brauseri <input type="date"> kuvab kuupäeva brauseri keele järgi
// (nt USA kujul kk/pp/aaaa) ja seda leht muuta ei saa — seepärast on nähtav väli tekstiväli ja
// kalendrinupp avab peidetud natiivse kuupäevavalija. Väärtus (prop date, emit) on ISO kujul "2026-10-05";
// poolik või vigane tekst → "".
const DATE_PATTERN = /^(\d{1,2})[./-](\d{1,2})[./-](\d{4})$/

function toIsoDate(text) {
  const match = DATE_PATTERN.exec(text.trim())
  if (!match) {
    return ''
  }
  const [, day, month, year] = match
  const isoDate = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`
  // Kontroll, et kuupäev on olemas (nt 31/02 ei ole)
  const date = new Date(`${isoDate}T00:00:00`)
  return date.getDate() === Number(day) && date.getMonth() + 1 === Number(month) ? isoDate : ''
}

export default {
  name: 'DateInput',
  components: { PhCalendarBlank },
  props: {
    date: String,
    inputId: String,
  },
  emits: ['event-new-date-input'],
  data() {
    return {
      text: FormatService.formatLocalDate(this.date),
    }
  },
  watch: {
    // Väärtus muutus väljastpoolt (andmete laadimine, kalendrivalik) → näita seda pp/kk/aaaa kujul.
    // Trükkimise ajal (poolik tekst → "") teksti üle ei kirjutata.
    date(newDate) {
      if (newDate !== toIsoDate(this.text) && newDate !== '') {
        this.text = FormatService.formatLocalDate(newDate)
      } else if (newDate === '' && toIsoDate(this.text) !== '') {
        this.text = ''
      }
    },
  },
  methods: {
    handleTextInput(text) {
      this.text = text
      this.$emit('event-new-date-input', toIsoDate(text))
    },

    handlePickerChange(isoDate) {
      this.text = FormatService.formatLocalDate(isoDate)
      this.$emit('event-new-date-input', isoDate)
    },

    openPicker() {
      const picker = this.$refs.picker
      if (typeof picker.showPicker === 'function') {
        picker.showPicker()
      } else {
        picker.focus()
      }
    },
  },
}
</script>

<template>
  <div class="relative flex">
    <input
      :value="text"
      @input="handleTextInput($event.target.value)"
      :id="inputId"
      :placeholder="$t('dateInput.placeholder')"
      class="form-control rounded-r-none"
      type="text"
      inputmode="numeric"
      autocomplete="off"
    />
    <button
      @click="openPicker"
      :title="$t('dateInput.openCalendar')"
      :aria-label="$t('dateInput.openCalendar')"
      class="btn btn-outline-secondary -ml-px rounded-l-none border-brand-200 px-3"
      type="button"
    >
      <PhCalendarBlank :size="18" />
    </button>
    <!-- Natiivne valija on nähtamatu; showPicker avab selle nupu juures -->
    <input
      ref="picker"
      :value="date"
      @change="handlePickerChange($event.target.value)"
      class="date-picker"
      type="date"
      tabindex="-1"
      aria-hidden="true"
    />
  </div>
</template>

<style scoped>
.date-picker {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
}
</style>
