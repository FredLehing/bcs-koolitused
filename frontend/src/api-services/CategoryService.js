// MOCK — võta import kommentaarist välja koos esimese päris kutsega:
// import axios from 'axios'
import { mockResponse } from '@/api-services/mock/mockResponse.js'
import MockDatabase from '@/api-services/mock/MockDatabase.js'

export default {
  // Backend task: docs/tasks/backend/GET-api-categories.md
  sendGetCategoriesRequest(contentLang) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.get('/api/categories', {
    //   params: {
    //     contentLang: contentLang,
    //   },
    // })
    return mockResponse(MockDatabase.getCategories(contentLang))
  },
}
