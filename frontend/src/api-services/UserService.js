import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/POST-api-user.md
  // signupRequest: { firstName, lastName, email, phone, password }; vastus nagu sisselogimisel
  sendPostUserRequest(signupRequest) {
    return axios.post('/api/user', signupRequest)
  },

  // Backend task: docs/tasks/backend/GET-api-user-userId-participant.md
  sendGetMyParticipantRequest(userId) {
    return axios.get(`/api/user/${userId}/participant`)
  },
}
