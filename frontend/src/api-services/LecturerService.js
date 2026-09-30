import axios from 'axios'
import { mockResponse } from '@/api-services/mock/mockResponse.js'

// AI tõlke mock: kuni backend teenus (GET /api/lecturer/{id}/ai-translation) valmib, võetakse
// salvestatud põhikeele tõlge päris teenustest ja lisatakse ametinimetusele ja lühikirjeldusele
// keele eesliide (sama muster nagu koolituse AI tõlke mockil). description jääb muutmata.
function createMockAiTranslation(lecturerId, languageId) {
  let languageCode = ''
  return axios
    .get('/api/languages')
    .then((response) => {
      languageCode = response.data.find(
        (language) => language.languageId === languageId,
      ).languageCode
      return axios.get(`/api/lecturer/${lecturerId}/lecturer-translations`)
    })
    .then((response) => {
      const mainTranslation = response.data.find(
        (lecturerTranslation) => lecturerTranslation.isMainLanguage,
      )
      return axios.get(`/api/lecturer-translation/${mainTranslation.lecturerTranslationId}`)
    })
    .then((response) => {
      const prefix = `[AI ${languageCode}] `
      return mockResponse(
        {
          title: prefix + response.data.title,
          shortDescription: prefix + response.data.shortDescription,
          description: response.data.description,
        },
        1200,
      )
    })
}

export default {
  // Backend task: docs/tasks/backend/GET-api-lecturers.md (ainult aktiivsed — lecturer-deleted-status.md)
  sendGetLecturersRequest(search) {
    return axios.get('/api/lecturers', {
      params: {
        search: search,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-admin-lecturers.md
  sendGetAdminLecturersRequest(contentLang, includeDeleted) {
    return axios.get('/api/admin-lecturers', {
      params: {
        contentLang: contentLang,
        includeDeleted: includeDeleted,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-lecturer-lecturerId.md
  sendGetLecturerRequest(lecturerId) {
    return axios.get(`/api/lecturer/${lecturerId}`)
  },

  // Backend task: docs/tasks/backend/GET-api-lecturer-lecturerId-lecturer-translations.md
  sendGetLecturerTranslationsRequest(lecturerId) {
    return axios.get(`/api/lecturer/${lecturerId}/lecturer-translations`)
  },

  // Backend task: docs/tasks/backend/POST-api-lecturer.md
  sendPostLecturerRequest(lecturerCreateRequest) {
    return axios.post('/api/lecturer', lecturerCreateRequest)
  },

  // Backend task: docs/tasks/backend/PUT-api-lecturer-lecturerId.md
  sendPutLecturerRequest(lecturerId, lecturerUpdateRequest) {
    return axios.put(`/api/lecturer/${lecturerId}`, lecturerUpdateRequest)
  },

  // Backend task: docs/tasks/backend/POST-api-lecturer-lecturerId-lecturer-translation.md
  sendPostLecturerTranslationRequest(lecturerId, lecturerTranslationCreateRequest) {
    return axios.post(
      `/api/lecturer/${lecturerId}/lecturer-translation`,
      lecturerTranslationCreateRequest,
    )
  },

  // Backend task: docs/tasks/backend/DELETE-api-lecturer-lecturerId.md
  sendDeleteLecturerRequest(lecturerId) {
    return axios.delete(`/api/lecturer/${lecturerId}`)
  },

  // Backend task: docs/tasks/backend/PUT-api-lecturer-lecturerId-restore.md
  sendPutLecturerRestoreRequest(lecturerId) {
    return axios.put(`/api/lecturer/${lecturerId}/restore`)
  },

  // Backend task: docs/tasks/backend/GET-api-lecturer-summary-lecturerId.md
  sendGetLecturerSummaryRequest(lecturerId, contentLang) {
    return axios.get(`/api/lecturer-summary/${lecturerId}`, {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-lecturer-summaries.md
  sendGetLecturerSummariesRequest(contentLang) {
    return axios.get('/api/lecturer-summaries', {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-lecturer-profile-lecturerId.md
  sendGetLecturerProfileRequest(lecturerId, contentLang) {
    return axios.get(`/api/lecturer-profile/${lecturerId}`, {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-lecturer-lecturerId-ai-translation.md (tööde järjekorras "Hiljem")
  sendGetAiTranslationRequest(lecturerId, languageId) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.get(`/api/lecturer/${lecturerId}/ai-translation`, {
    //   params: {
    //     languageId: languageId,
    //   },
    // })
    return createMockAiTranslation(lecturerId, languageId)
  },

  // Backend task: docs/tasks/backend/GET-api-lecturer-lecturerId-photo.md
  // Pildi URL <img src> jaoks; photoVersion muutub pildi vahetamisel → brauser ei näita vana pilti
  getLecturerPhotoUrl(lecturerId, photoVersion) {
    return `/api/lecturer/${lecturerId}/photo?v=${photoVersion}`
  },
}
