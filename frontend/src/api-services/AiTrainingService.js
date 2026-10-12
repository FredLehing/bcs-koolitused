import axios from 'axios'

function createCurriculumFormData(curriculumFile) {
  const formData = new FormData()
  if (curriculumFile) {
    formData.append('curriculum', curriculumFile, curriculumFile.name)
  }
  return formData
}

export default {
  sendPostPdfRequest(curriculumFile) {
    return axios.post('/api/ai-training/pdf', createCurriculumFormData(curriculumFile))
  },

  sendPostSavedCurriculumRequest(trainingTranslationId) {
    return axios.post(`/api/ai-training/pdf/${trainingTranslationId}`)
  },

  sendPostTranslationRequest(trainingId, languageId) {
    return axios.post(`/api/ai-training/translation/${trainingId}`, null, {
      params: { languageId: languageId },
    })
  },
}
