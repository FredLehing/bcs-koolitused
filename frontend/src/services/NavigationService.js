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

  navigateToLoginView() {
    router.push({ name: 'loginRoute' })
  },

  navigateToNotAuthorizedView() {
    router.push({ name: 'notAuthorizedRoute' })
  },

  navigateToTrainingView() {
    router.push({ name: 'trainingRoute' })
  },

  // Vahetab TrainingFormView oleku (query parameetrid) ilma brauseri ajalukku uut kirjet lisamata
  replaceTrainingFormView(query) {
    router.replace({ name: 'trainingFormRoute', query: query })
  },
}
