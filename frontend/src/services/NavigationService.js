import router from '@/router/index.js'

export default {
  navigateToTrainingsView() {
    router.push({ name: 'trainingsRoute' })
  },

  navigateToErrorView() {
    router.push({ name: 'errorRoute' })
  },

  navigateToHomeView() {
    router.push({ name: 'homeRoute' })
  },
}
