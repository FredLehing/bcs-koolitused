export default {
  getUserId() {
    return Number(sessionStorage.getItem('userId'))
  },

  userIsAdmin() {
    return sessionStorage.getItem('roleName') === 'admin'
  },

  userIsLoggedIn() {
    return sessionStorage.getItem('userId') !== null
  },

  clearSession() {
    sessionStorage.removeItem('userId')
    sessionStorage.removeItem('roleName')
  },
}
