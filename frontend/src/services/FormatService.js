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

  // 490 → "490,00" (et) / "490.00" (en)
  formatPrice(price, contentLang) {
    return new Intl.NumberFormat(contentLang, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(price)
  },
}
