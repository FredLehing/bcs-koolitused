// MOCK — võta import kommentaarist välja koos esimese päris kutsega:
// import axios from 'axios'
import { mockResponse } from '@/api-services/mock/mockResponse.js'
import MockDatabase from '@/api-services/mock/MockDatabase.js'

export default {
  // Backend task: docs/tasks/backend/GET-api-lecturers.md
  sendGetLecturersRequest(search) {
    // MOCK — vaheta päris kutse vastu, kui teenus on valmis:
    // return axios.get('/api/lecturers', {
    //   params: {
    //     search: search,
    //   },
    // })
    return mockResponse(MockDatabase.getLecturers(search))
  },
}
