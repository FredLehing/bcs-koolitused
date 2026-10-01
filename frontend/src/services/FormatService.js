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

  // Toimumiskorra kaardi kuupäevaplokk: et "05.–09. okt 2026", en "5–9 Oct 2026";
  // eri kuud "28. okt – 03. nov 2026" / "28 Oct – 3 Nov 2026", eri aastad mõlemal aasta
  formatCourseDates(startDate, endDate, contentLang) {
    const [startYear, startMonth, startDay] = startDate.split('-')
    const [endYear, endMonth, endDay] = endDate.split('-')
    const monthName = (month) =>
      new Intl.DateTimeFormat(contentLang, { month: 'short', timeZone: 'UTC' })
        .format(new Date(Date.UTC(2000, Number(month) - 1, 1)))
        .replace('.', '')
    const isEstonian = contentLang === 'et'
    const day = (dayText) => (isEstonian ? `${dayText}.` : String(Number(dayText)))
    const dayMonth = (dayText, month) => `${day(dayText)} ${monthName(month)}`
    if (startDate === endDate) {
      return `${dayMonth(startDay, startMonth)} ${startYear}`
    }
    if (startYear !== endYear) {
      return `${dayMonth(startDay, startMonth)} ${startYear} – ${dayMonth(endDay, endMonth)} ${endYear}`
    }
    if (startMonth !== endMonth) {
      return `${dayMonth(startDay, startMonth)} – ${dayMonth(endDay, endMonth)} ${endYear}`
    }
    return `${day(startDay)}–${day(endDay)} ${monthName(endMonth)} ${endYear}`
  },

  // 490 → "490,00" (et) / "490.00" (en)
  formatPrice(price, contentLang) {
    return new Intl.NumberFormat(contentLang, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(price)
  },
}
