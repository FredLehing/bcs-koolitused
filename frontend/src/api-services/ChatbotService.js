import axios from 'axios'

export default {
  sendQuestionRequest(question) {
    return axios.post('/api/ask', {
      question: question,
    })
  },
}
