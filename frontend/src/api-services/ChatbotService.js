import axios from 'axios'

export default {
  sendQuestionRequest(question, language, previousMessages) {
    return axios.post('/api/chatbot/ask', {
      question,
      language,
      previousMessages,
    })
  },
}
