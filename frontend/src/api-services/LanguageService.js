import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-languages.md
  sendGetLanguagesRequest() {
    return axios.get('/api/languages')
  },
}
