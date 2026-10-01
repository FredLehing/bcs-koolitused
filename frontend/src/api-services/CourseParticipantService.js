import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-admin-registrations.md
  sendGetAdminRegistrationsRequest(contentLang, includeCancelled, includePast) {
    return axios.get('/api/admin-registrations', {
      params: {
        contentLang: contentLang,
        includeCancelled: includeCancelled,
        includePast: includePast,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-admin-registration-courseParticipantId.md
  sendGetAdminRegistrationRequest(courseParticipantId, contentLang) {
    return axios.get(`/api/admin-registration/${courseParticipantId}`, {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/PUT-api-admin-registration-courseParticipantId.md
  sendPutAdminRegistrationRequest(courseParticipantId, adminRegistrationUpdateRequest) {
    return axios.put(
      `/api/admin-registration/${courseParticipantId}`,
      adminRegistrationUpdateRequest,
    )
  },
}
