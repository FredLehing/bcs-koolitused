import axios from 'axios'

export default {
  sendLoginRequest(email, password) {
    return axios.post('/api/login', {
      email: email,
      password: password,
    })
  },
}
