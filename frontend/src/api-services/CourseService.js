import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-training-trainingId-courses.md
  sendGetTrainingCoursesRequest(trainingId, includePast) {
    return axios.get(`/api/training/${trainingId}/courses`, {
      params: {
        includePast: includePast,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-course-courseId.md
  sendGetCourseRequest(courseId) {
    return axios.get(`/api/course/${courseId}`)
  },

  // Backend task: docs/tasks/backend/POST-api-training-trainingId-course.md
  sendPostCourseRequest(trainingId, courseCreateRequest) {
    return axios.post(`/api/training/${trainingId}/course`, courseCreateRequest)
  },

  // Backend task: docs/tasks/backend/PUT-api-course-courseId.md
  sendPutCourseRequest(courseId, courseUpdateRequest) {
    return axios.put(`/api/course/${courseId}`, courseUpdateRequest)
  },

  // Backend task: docs/tasks/backend/DELETE-api-course-courseId.md
  sendDeleteCourseRequest(courseId) {
    return axios.delete(`/api/course/${courseId}`)
  },

  // Backend task: docs/tasks/backend/GET-api-admin-courses.md
  // adminCourseFilter: { contentLang, searchText, categoryId, trainingLanguageId, status, attendance,
  // isPromoted, startDateFrom, startDateTo, includePast, sortBy, sortDirection, page, limit };
  // null väärtusi axios päringusse ei lisa (= filtrit ei rakendata)
  sendGetAdminCoursesRequest(adminCourseFilter) {
    return axios.get('/api/admin-courses', {
      params: adminCourseFilter,
    })
  },

  // Backend task: docs/tasks/backend/GET-api-admin-course-courseId.md
  sendGetAdminCourseRequest(courseId, contentLang) {
    return axios.get(`/api/admin-course/${courseId}`, {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-course-courseId-participants.md
  sendGetCourseParticipantsRequest(courseId) {
    return axios.get(`/api/course/${courseId}/participants`)
  },

  // Backend task: docs/tasks/backend/GET-api-courses.md
  // publicCourseFilter: { contentLang, searchText, categoryId, trainingLanguageId, fundingTypeId,
  // attendance, hideFull, startDateFrom, startDateTo, page, limit }
  sendGetCoursesRequest(publicCourseFilter) {
    return axios.get('/api/courses', {
      params: publicCourseFilter,
    })
  },

  // Backend task: docs/tasks/backend/GET-api-next-courses.md
  sendGetNextCoursesRequest(contentLang, limit) {
    return axios.get('/api/next-courses', {
      params: {
        contentLang: contentLang,
        limit: limit,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-course-summary-courseId.md
  sendGetCourseSummaryRequest(courseId, contentLang) {
    return axios.get(`/api/course-summary/${courseId}`, {
      params: {
        contentLang: contentLang,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-course-courseId-participant-status.md
  sendGetCourseParticipantStatusRequest(courseId, userId) {
    return axios.get(`/api/course/${courseId}/participant-status`, {
      params: {
        userId: userId,
      },
    })
  },

  // Backend task: docs/tasks/backend/POST-api-course-courseId-participant.md
  sendPostCourseParticipantRequest(courseId, courseRegistrationRequest) {
    return axios.post(`/api/course/${courseId}/participant`, courseRegistrationRequest)
  },
}
