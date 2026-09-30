import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-rooms.md
  sendGetRoomsRequest() {
    return axios.get('/api/rooms')
  },
}
