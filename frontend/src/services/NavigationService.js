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

  navigateToAdminLecturersView() {
    router.push({ name: 'adminLecturersRoute' })
  },

  // query: {} (uus koolitaja) või { lecturerId, lecturerTranslationId }
  navigateToLecturerFormView(query) {
    router.push({ name: 'lecturerFormRoute', query: query })
  },

  // Vahetab LecturerFormView oleku (query parameetrid) ilma brauseri ajalukku uut kirjet lisamata
  replaceLecturerFormView(query) {
    router.replace({ name: 'lecturerFormRoute', query: query })
  },

  // successMessage (valikuline) antakse kalendrile edasi history state'is — nt vormi eduteade
  navigateToAdminTrainingCoursesView(trainingId, successMessage) {
    router.push({
      name: 'adminTrainingCoursesRoute',
      query: { trainingId: trainingId },
      state: successMessage ? { successMessage: successMessage } : undefined,
    })
  },

  // query: { trainingId } (uus toimumiskord) või { courseId } (muutmine)
  navigateToCourseFormView(query) {
    router.push({ name: 'courseFormRoute', query: query })
  },

  // successMessage (valikuline) antakse nimekirjale edasi history state'is — nt vormi eduteade
  navigateToAdminRoomsView(successMessage) {
    router.push({
      name: 'adminRoomsRoute',
      state: successMessage ? { successMessage: successMessage } : undefined,
    })
  },

  // query: {} (uus ruum) või { roomId } (muutmine)
  navigateToRoomFormView(query) {
    router.push({ name: 'roomFormRoute', query: query })
  },

  navigateToAdminEnquiriesView() {
    router.push({ name: 'adminEnquiriesRoute' })
  },

  navigateToLecturersView() {
    router.push({ name: 'lecturersRoute' })
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
