// Frontendi tabelite sorteerimine (kui kogu nimekiri on juba laaditud, nt toimumiskorra osalejad)

export default {
  // Tagastab uue massiivi; tekst tähestiku järgi (contentLang), arvud ja tõeväärtused suuruse järgi,
  // null / undefined alati lõpus. sortDirection: 'asc' / 'desc'
  sortRows(rows, sortKey, sortDirection, contentLang) {
    const direction = sortDirection === 'desc' ? -1 : 1
    return [...rows].sort((rowA, rowB) => {
      const valueA = rowA[sortKey]
      const valueB = rowB[sortKey]
      if (valueA == null || valueB == null) {
        return valueA == null ? (valueB == null ? 0 : 1) : -1
      }
      if (typeof valueA === 'string') {
        return direction * valueA.localeCompare(valueB, contentLang, { sensitivity: 'base' })
      }
      return direction * (Number(valueA) - Number(valueB))
    })
  },
}
