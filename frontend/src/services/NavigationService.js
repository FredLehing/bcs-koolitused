import router from '@/router/index.js'

export default {
  navigateToTrainingsView() {
    router.push({ name: 'trainingsRoute' })
  },

  navigateToErrorView() {
    // todo: arenduse ajal välja kommenteeritud
    // router.push({ name: 'errorRoute' })
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

  navigateToTrainingView(trainingId) {
    router.push({ name: 'trainingRoute', query: { trainingId: trainingId } })
  },

  // Vahetab TrainingFormView oleku (query parameetrid) ilma brauseri ajalukku uut kirjet lisamata
  replaceTrainingFormView(query) {
    router.replace({ name: 'trainingFormRoute', query: query })
  },
}
