import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-admin-enquiries.md
  sendGetAdminEnquiriesRequest(contentLang, includeHandled) {
    return axios.get('/api/admin-enquiries', {
      params: {
        contentLang: contentLang,
        includeHandled: includeHandled,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-admin-enquiry-enquiryId.md
  sendGetAdminEnquiryRequest(enquiryId, contentLang) {
    return axios.get(`/api/admin-enquiry/${enquiryId}`, {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/PUT-api-enquiry-enquiryId-handle.md
  sendPutEnquiryHandleRequest(enquiryId) {
    return axios.put(`/api/enquiry/${enquiryId}/handle`)
  },

  // Backend task: docs/tasks/backend/PUT-api-enquiry-enquiryId-handle.md
  sendPutEnquiryReopenRequest(enquiryId) {
    return axios.put(`/api/enquiry/${enquiryId}/reopen`)
  },
}
