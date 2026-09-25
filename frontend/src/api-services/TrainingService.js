import axios from 'axios'


export default {
  sendGetTrainingsRequest(categoryId, fundingTypeId, limit, page, trainingLanguageId, contentLang) {
    return axios.get('/api/trainings', {
      params: {
        categoryId: categoryId,
        fundingTypeId: fundingTypeId,
        limit: limit,
        page: page,
        trainingLanguageId: trainingLanguageId,
        contentLang: contentLang,
      },
    })
  },
}

