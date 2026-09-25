import axios from 'axios'


export default {
  sendGetTrainingsRequest(categoryId, fundingTypeId, limit, page, trainingLang, contentLang) {
    return axios.get('/api/trainings', {
      params: {
        categoryId: categoryId,
        fundingTypeId: fundingTypeId,
        limit: limit,
        page: page,
        trainingLang: trainingLang,
        contentLang: contentLang,
      },
    })
  },
}

