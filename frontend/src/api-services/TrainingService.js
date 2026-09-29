import axios from 'axios'
import { mockResponse } from '@/api-services/mock/mockResponse.js'
import MockDatabase from '@/api-services/mock/MockDatabase.js'

export default {
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
  // NB! Backend on valmis, aga vaheta see koos GET /api/training/{id}, .../training-translations ja
  // GET /api/training-translation/{id} kutsetega (3. etapp) — "Lisa" järel laaditakse uus koolitus nende kaudu,
  // mock-andmebaasis päris andmebaasi koolitust pole.
  sendPostTrainingRequest(trainingCreateRequest) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.post('/api/training', trainingCreateRequest)
    return mockResponse(MockDatabase.addTraining(trainingCreateRequest))
  },

  // Backend task: docs/tasks/backend/GET-api-training-trainingId.md (3. etapp)
  sendGetTrainingRequest(trainingId) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.get(`/api/training/${trainingId}`)
    return mockResponse(MockDatabase.getTraining(trainingId))
  },

  // Backend task: docs/tasks/backend/GET-api-training-trainingId-training-translations.md (3. etapp)
  sendGetTrainingTranslationsRequest(trainingId) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.get(`/api/training/${trainingId}/training-translations`)
    return mockResponse(MockDatabase.getTrainingTranslations(trainingId))
  },

  // Backend task: docs/tasks/backend/PUT-api-training-trainingId.md (3. etapp)
  sendPutTrainingRequest(trainingId, trainingUpdateRequest) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.put(`/api/training/${trainingId}`, trainingUpdateRequest)
    return mockResponse(MockDatabase.updateTraining(trainingId, trainingUpdateRequest))
  },

  // Backend task: docs/tasks/backend/PUT-api-training-trainingId-publish.md (3. etapp)
  sendPutTrainingPublishRequest(trainingId) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.put(`/api/training/${trainingId}/publish`)
    return mockResponse(MockDatabase.setTrainingStatus(trainingId, 'P'))
  },

  // Backend task: docs/tasks/backend/PUT-api-training-trainingId-unpublish.md (3. etapp)
  sendPutTrainingUnpublishRequest(trainingId) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.put(`/api/training/${trainingId}/unpublish`)
    return mockResponse(MockDatabase.setTrainingStatus(trainingId, 'U'))
  },

  // Backend task: docs/tasks/backend/POST-api-training-trainingId-training-translation.md (3. etapp)
  sendPostTrainingTranslationRequest(trainingId, trainingTranslationCreateRequest) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.post(`/api/training/${trainingId}/training-translation`, trainingTranslationCreateRequest)
    return mockResponse(
      MockDatabase.addTrainingTranslation(trainingId, trainingTranslationCreateRequest),
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
