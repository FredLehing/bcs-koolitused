import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-funding-types.md
  sendGetFundingTypesRequest(contentLang) {
    return axios.get('/api/funding-types', {
      params: {
        contentLang: contentLang,
      },
    })
  },
}
