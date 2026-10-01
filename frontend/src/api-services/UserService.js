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

  // Backend task: docs/tasks/backend/PUT-api-user-userId-profile.md
  // profileUpdateRequest: { firstName, lastName, email, phone }; e-post muutub ka sisselogimiseks
  sendPutProfileRequest(userId, profileUpdateRequest) {
    return axios.put(`/api/user/${userId}/profile`, profileUpdateRequest)
  },

  // Backend task: docs/tasks/backend/GET-api-user-userId-registrations.md
  sendGetMyRegistrationsRequest(userId, contentLang) {
    return axios.get(`/api/user/${userId}/registrations`, {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/PUT-api-user-userId-registration-courseParticipantId-cancel.md
  sendPutCancelRegistrationRequest(userId, courseParticipantId) {
    return axios.put(`/api/user/${userId}/registration/${courseParticipantId}/cancel`)
  },

  // Backend task: docs/tasks/backend/PUT-api-user-userId-password.md
  // passwordChangeRequest: { currentPassword, newPassword }
  sendPutPasswordRequest(userId, passwordChangeRequest) {
    return axios.put(`/api/user/${userId}/password`, passwordChangeRequest)
  },

  // Backend task: docs/tasks/backend/GET-api-admin-users.md
  sendGetAdminUsersRequest(includeDeleted) {
    return axios.get('/api/admin-users', {
      params: {
        includeDeleted: includeDeleted,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-admin-user-userId.md
  sendGetAdminUserRequest(userId, contentLang) {
    return axios.get(`/api/admin-user/${userId}`, {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/DELETE-api-admin-user-userId.md
  // currentUserId: sisse loginud admin (iseennast deaktiveerida ei saa)
  sendDeleteAdminUserRequest(userId, currentUserId) {
    return axios.delete(`/api/admin-user/${userId}`, {
      params: {
        currentUserId: currentUserId,
      },
    })
  },

  // Backend task: docs/tasks/backend/PUT-api-admin-user-userId-restore.md
  sendPutAdminUserRestoreRequest(userId) {
    return axios.put(`/api/admin-user/${userId}/restore`)
  },
}
