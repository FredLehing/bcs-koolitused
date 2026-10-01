import router from '@/router/index.js'

function createTrainingViewQuery(trainingId, trainingTranslationId) {
  return trainingTranslationId
    ? { trainingId: trainingId, trainingTranslationId: trainingTranslationId }
    : { trainingId: trainingId }
}

// Ainult sama rakenduse rada ("/..."), mitte välisaadress ("//...", "/\\..." või "https://...")
function isInternalPath(path) {
  return (
    typeof path === 'string' &&
    path.startsWith('/') &&
    !path.startsWith('//') &&
    !/[\\\u0000-\u001f\u007f]/.test(path)
  )
}

function getReturnTo(returnTo, currentPath = router.currentRoute.value.fullPath) {
  if (!isInternalPath(returnTo)) return ''
  const target = router.resolve(returnTo)
  if (target.matched.length === 0) return ''
  const current = router.resolve(currentPath)
  const queryWithoutReturnTo = (query) =>
    Object.fromEntries(
      Object.entries(query)
        .filter(([key]) => key !== 'returnTo')
        .sort(),
    )
  if (
    target.path === current.path &&
    JSON.stringify(queryWithoutReturnTo(target.query)) ===
      JSON.stringify(queryWithoutReturnTo(current.query))
  )
    return ''
  return target.fullPath
}

function withReturnTo(query = {}, returnTo = router.currentRoute.value.fullPath) {
  return isInternalPath(returnTo) ? { ...query, returnTo } : { ...query }
}

function preserveReturnTo(query) {
  const returnTo = getReturnTo(router.currentRoute.value.query.returnTo)
  return returnTo ? { ...query, returnTo } : query
}

export default {
  getReturnTo,
  withReturnTo,

  // Tagasi on päris sihtlink, mitte sõltuvus brauseri ajaloost. Edukas salvestamine võib kaasa
  // anda teate; kustutamisel saab välistada detailvaate, mille kirjet enam pole.
  navigateBack(fallback, successMessage, excludedRouteNames = []) {
    const returnTo = getReturnTo(router.currentRoute.value.query.returnTo)
    const target = router.resolve(returnTo || fallback)
    const destination = excludedRouteNames.includes(target.name) ? router.resolve(fallback) : target
    return router.push({
      path: destination.path,
      query: destination.query,
      hash: destination.hash,
      state: successMessage ? { successMessage } : undefined,
    })
  },
  // Kasutaja antud tagasitee (redirect, returnTo) tohib viia ainult sama rakenduse sisse
  isInternalPath(path) {
    return isInternalPath(path)
  },

  navigateToTrainingsView(query) {
    router.push({ name: 'trainingsRoute', query: query })
  },

  navigateToErrorView() {
    // todo: arenduse ajal välja kommenteeritud
    // router.push({ name: 'errorRoute' })
  },

  navigateToHomeView() {
    router.push({ name: 'homeRoute' })
  },

  // redirect (valikuline): rada, kuhu pärast sisselogimist või konto loomist minnakse,
  // nt "/course-registration?courseId=9"
  navigateToLoginView(redirect) {
    router.push({ name: 'loginRoute', query: redirect ? { redirect: redirect } : {} })
  },

  // successMessage (valikuline) antakse lehele edasi history state'is — nt registreerumise eduteade
  navigateToCourseView(courseId, successMessage) {
    router.push({
      name: 'courseRoute',
      query: { courseId: courseId },
      state: successMessage ? { successMessage: successMessage } : undefined,
    })
  },

  navigateToCourseRegistrationView(courseId) {
    router.push({ name: 'courseRegistrationRoute', query: withReturnTo({ courseId: courseId }) })
  },

  // Pärast sisselogimist / konto loomist: redirect (ainult sisemine rada, algab "/"-ga) või avaleht
  navigateToRedirectOrHomeView(redirect) {
    if (isInternalPath(redirect)) {
      router.push(redirect)
    } else {
      router.push({ name: 'homeRoute' })
    }
  },

  navigateToSignupView(redirect) {
    router.push({ name: 'signupRoute', query: redirect ? { redirect: redirect } : {} })
  },

  navigateToAdminTrainingsView() {
    router.push({ name: 'adminTrainingsRoute' })
  },

  navigateToTrainingFormView() {
    router.push({ name: 'trainingFormRoute', query: withReturnTo() })
  },

  navigateToAdminLecturersView() {
    router.push({ name: 'adminLecturersRoute' })
  },

  // query: {} (uus koolitaja) või { lecturerId, lecturerTranslationId }
  navigateToLecturerFormView(query) {
    router.push({ name: 'lecturerFormRoute', query: withReturnTo(query) })
  },

  // Vahetab LecturerFormView oleku (query parameetrid) ilma brauseri ajalukku uut kirjet lisamata
  replaceLecturerFormView(query) {
    router.replace({ name: 'lecturerFormRoute', query: preserveReturnTo(query) })
  },

  // successMessage (valikuline) antakse kalendrile edasi history state'is — nt vormi eduteade
  navigateToAdminTrainingCoursesView(trainingId, successMessage) {
    router.push({
      name: 'adminTrainingCoursesRoute',
      query: withReturnTo({ trainingId: trainingId }),
      state: successMessage ? { successMessage: successMessage } : undefined,
    })
  },

  // query: { trainingId } (uus toimumiskord) või { courseId } (muutmine)
  navigateToCourseFormView(query) {
    router.push({ name: 'courseFormRoute', query: withReturnTo(query) })
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
    router.push({ name: 'roomFormRoute', query: withReturnTo(query) })
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
      query: withReturnTo(createTrainingViewQuery(trainingId, trainingTranslationId)),
    })
  },

  replaceTrainingView(trainingId) {
    router.replace({
      name: 'trainingRoute',
      query: preserveReturnTo(createTrainingViewQuery(trainingId)),
    })
  },

  // Vahetab TrainingFormView oleku (query parameetrid) ilma brauseri ajalukku uut kirjet lisamata
  replaceTrainingFormView(query) {
    router.replace({ name: 'trainingFormRoute', query: preserveReturnTo(query) })
  },
}
