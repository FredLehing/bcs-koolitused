// Kuvamise vormingud, mida kasutavad mitu vaadet

export default {
  // LocalDate ("2026-10-05") → "05/10/2026"; ajavööndit ei arvestata (kuupäev on kuupäev)
  formatLocalDate(localDate) {
    if (!localDate) {
      return ''
    }
    const [year, month, day] = localDate.split('-')
    return `${day}/${month}/${year}`
  },

  // Ajatempel (Instant) → "30/09/2026 14:20" kasutaja ajavööndis
  formatDateTime(instant) {
    if (!instant) {
      return ''
    }
    const date = new Date(instant)
    const pad = (number) => String(number).padStart(2, '0')
    return `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()} ${pad(date.getHours())}:${pad(date.getMinutes())}`
  },

  // Toimumiskorra kuupäevad → "05/10/2026 – 09/10/2026"; toimumiskorra puudumisel ""
  formatDateRange(startDate, endDate) {
    if (!startDate) {
      return ''
    }
    return `${this.formatLocalDate(startDate)} – ${this.formatLocalDate(endDate)}`
  },

  // 490 → "490,00" (et) / "490.00" (en)
  formatPrice(price, contentLang) {
    return new Intl.NumberFormat(contentLang, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(price)
  },
}
