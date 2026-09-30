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
}
