import axios from 'axios'
import { mockResponse } from '@/api-services/mock/mockResponse.js'
import MockDatabase from '@/api-services/mock/MockDatabase.js'

export default {
  getCurriculumUrl(trainingTranslationId) {
    return `/api/training-translation/${trainingTranslationId}/curriculum`
  },

  sendGetTrainingsRequest(
    categoryId,
    fundingTypeId,
    limit,
    page,
    trainingLanguageId,
    contentLang,
    searchText,
  ) {
    return axios.get('/api/trainings', {
      params: {
        categoryId: categoryId,
        fundingTypeId: fundingTypeId,
        limit: limit,
        page: page,
        trainingLanguageId: trainingLanguageId,
        contentLang: contentLang,
        searchText: searchText,
      },
    })
  },

  // Backend task: docs/tasks/backend/POST-api-training.md
  // Vahetatud päris kutsele koos GET /api/training/{id}, .../training-translations ja
  // GET /api/training-translation/{id} kutsetega — "Lisa" järel laaditakse uus koolitus nende kaudu.
  sendPostTrainingRequest(trainingCreateRequest) {
    return axios.post('/api/training', trainingCreateRequest)
  },

  // Backend task: docs/tasks/backend/GET-api-training-trainingId.md (3. etapp)
  sendGetTrainingRequest(trainingId) {
    return axios.get(`/api/training/${trainingId}`)
  },

  // Backend task: docs/tasks/backend/GET-api-admin-training-trainingId.md
  sendGetAdminTrainingRequest(trainingId, contentLang) {
    return axios.get(`/api/admin-training/${trainingId}`, {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-training-trainingId-training-translations.md (3. etapp)
  sendGetTrainingTranslationsRequest(trainingId) {
    return axios.get(`/api/training/${trainingId}/training-translations`)
  },

  // Backend task: docs/tasks/backend/PUT-api-training-trainingId.md (3. etapp)
  sendPutTrainingRequest(trainingId, trainingUpdateRequest) {
    return axios.put(`/api/training/${trainingId}`, trainingUpdateRequest)
  },

  // Backend task: docs/tasks/backend/PUT-api-training-trainingId-publish.md
  sendPutTrainingPublishRequest(trainingId) {
    return axios.put(`/api/training/${trainingId}/publish`)
  },

  // Backend task: docs/tasks/backend/PUT-api-training-trainingId-unpublish.md
  sendPutTrainingUnpublishRequest(trainingId) {
    return axios.put(`/api/training/${trainingId}/unpublish`)
  },

  // Backend task: docs/tasks/backend/PUT-api-training-trainingId-restore.md
  sendPutTrainingRestoreRequest(trainingId) {
    return axios.put(`/api/training/${trainingId}/restore`)
  },

  // Backend task: docs/tasks/backend/DELETE-api-training-trainingId.md
  sendDeleteTrainingRequest(trainingId) {
    return axios.delete(`/api/training/${trainingId}`)
  },

  // Backend task: docs/tasks/backend/GET-api-admin-trainings.md
  // adminTrainingFilter: contentLang, searchText, categoryId, trainingLanguageId, fundingTypeId,
  // status, isOrderable, isPromoted, hasAllTranslations, sortBy, sortDirection, page, limit.
  // null väärtusega parameetreid axios päringusse ei lisa (valikulised filtrid).
  sendGetAdminTrainingsRequest(adminTrainingFilter) {
    return axios.get('/api/admin-trainings', {
      params: adminTrainingFilter,
    })
  },

  // Backend task: docs/tasks/backend/GET-api-training-titles.md
  sendGetTrainingTitlesRequest(contentLang) {
    return axios.get('/api/training-titles', {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/POST-api-training-trainingId-training-translation.md (3. etapp)
  sendPostTrainingTranslationRequest(trainingId, trainingTranslationCreateRequest) {
    return axios.post(
      `/api/training/${trainingId}/training-translation`,
      trainingTranslationCreateRequest,
    )
  },

  // Backend task: docs/tasks/backend/GET-api-training-trainingId-ai-translation.md (3. etapp)
  sendGetAiTranslationRequest(trainingId, languageId) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.get(`/api/training/${trainingId}/ai-translation`, {
    //   params: {
    //     languageId: languageId,
    //   },
    // })
    return mockResponse(MockDatabase.getAiTranslation(trainingId, languageId), 1200)
  },
}
