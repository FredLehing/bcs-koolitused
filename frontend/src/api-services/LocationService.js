import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-locations.md
  sendGetLocationsRequest() {
    return axios.get('/api/locations')
  },
}
