import axios from 'axios'

export default {
  sendGetAdminFeedbackCoursesRequest(contentLang) {
    return axios.get('/api/admin-feedback-courses', { params: { contentLang } })
  },
  sendGetAdminFeedbacksRequest(parameters) {
    return axios.get('/api/admin-feedbacks', { params: parameters })
  },
  sendGetAdminFeedbackRequest(feedbackId, contentLang) {
    return axios.get(`/api/admin-feedback/${feedbackId}`, { params: { contentLang } })
  },
  sendPutAdminFeedbackReviewRequest(feedbackId, answersVersion) {
    return axios.put(`/api/admin-feedback/${feedbackId}/review`, null, {
      headers: { 'X-Answers-Version': answersVersion },
    })
  },
}
