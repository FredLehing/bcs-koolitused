import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-training-translation-trainingTranslationId.md (3. etapp)
  sendGetTrainingTranslationRequest(trainingTranslationId) {
    return axios.get(`/api/training-translation/${trainingTranslationId}`)
  },
}
