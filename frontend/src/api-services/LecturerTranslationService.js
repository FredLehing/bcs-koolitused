import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-lecturer-translation-lecturerTranslationId.md
  sendGetLecturerTranslationRequest(lecturerTranslationId) {
    return axios.get(`/api/lecturer-translation/${lecturerTranslationId}`)
  },
}
