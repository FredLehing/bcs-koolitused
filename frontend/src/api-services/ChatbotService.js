import axios from 'axios'

export default {
  sendQuestionRequest(question, language) {
    return axios.post('/api/chatbot/ask', {
      question,
      language,
    })
  },
}
