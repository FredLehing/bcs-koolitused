import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-categories.md
  sendGetCategoriesRequest(contentLang) {
    return axios.get('/api/categories', {
      params: {
        contentLang: contentLang,
      },
    })
  },
}
