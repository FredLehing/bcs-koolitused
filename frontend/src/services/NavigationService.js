import router from '@/router/index.js'

function createTrainingViewQuery(trainingId, trainingTranslationId) {
  return trainingTranslationId
    ? { trainingId: trainingId, trainingTranslationId: trainingTranslationId }
    : { trainingId: trainingId }
}

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

  navigateToAdminTrainingsView() {
    router.push({ name: 'adminTrainingsRoute' })
  },

  navigateToTrainingFormView() {
    router.push({ name: 'trainingFormRoute' })
  },

  navigateToNotAuthorizedView() {
    router.push({ name: 'notAuthorizedRoute' })
  },

  // trainingTranslationId on valikuline (kindla tõlke eelvaade); ilma selleta valitakse tõlge
  // kasutajaliidese keele järgi
  navigateToTrainingView(trainingId, trainingTranslationId) {
    router.push({
      name: 'trainingRoute',
      query: createTrainingViewQuery(trainingId, trainingTranslationId),
    })
  },

  replaceTrainingView(trainingId) {
    router.replace({ name: 'trainingRoute', query: createTrainingViewQuery(trainingId) })
  },

  // Vahetab TrainingFormView oleku (query parameetrid) ilma brauseri ajalukku uut kirjet lisamata
  replaceTrainingFormView(query) {
    router.replace({ name: 'trainingFormRoute', query: query })
  },
}
