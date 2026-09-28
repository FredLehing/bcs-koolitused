export default {
  getUserId() {
    return Number(sessionStorage.getItem('userId'))
  },

  userIsAdmin() {
    return sessionStorage.getItem('roleName') === 'admin'
  },
}
