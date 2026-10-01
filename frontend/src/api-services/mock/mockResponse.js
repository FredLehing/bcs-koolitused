// Mock-vastuse abimeetod.
// Tagastab Promise'i samal kujul nagu axios ({ data }), nii et vaate kood (.then((response) => response.data))
// töötab muutmata ka siis, kui mock asendatakse päris API kutsega.
//
// Kasutamine api-services failides:
//   // return axios.post(`/api/ai-training/translation/${trainingId}`, null, { params: { languageId: languageId } })
//   return mockResponse(MockDatabase.getAiTranslation(trainingId, languageId))
//
// Kui backendi teenus valmib: võta päris kutse kommentaarist välja ja kustuta mockResponse rida.
// Kui ükski teenus mocki enam ei kasuta, kustuta kogu kaust src/api-services/mock/.

export function mockResponse(data, delayMs = 300) {
  // Koopia, et vaade ei muudaks kogemata mock-andmebaasi objekte otse
  const copy = data === undefined ? undefined : JSON.parse(JSON.stringify(data))
  return new Promise((resolve) => setTimeout(() => resolve({ status: 200, data: copy }), delayMs))
}
