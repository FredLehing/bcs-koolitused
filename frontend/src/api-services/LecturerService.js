 import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-lecturers.md
  sendGetLecturersRequest(search) {
    return axios.get('/api/lecturers', {
      params: {
        search: search,
      },
    })
  },
}
