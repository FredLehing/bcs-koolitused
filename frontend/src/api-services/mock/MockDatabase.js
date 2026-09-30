// Ajutine mälupõhine "andmebaas" mock-vastuste jaoks.
// Algandmed vastavad docs/database/3_import.sql failile. POST/PUT mockid muudavad neid andmeid,
// et TrainingFormView voog (lisa koolitus → muuda → lisa tõlge) töötaks ka ilma backendita.
// Lehe värskendamisel (F5) taastuvad algandmed.
// Kustuta see fail, kui kõik teenused kasutavad päris API kutseid.

const languages = [
  {
    languageId: 1,
    languageCode: 'et',
    languageName: 'Eesti',
    isMainLanguage: true,
    requiresTranslation: true,
    flagIconCode: 'fi-ee',
  },
  {
    languageId: 2,
    languageCode: 'en',
    languageName: 'English',
    isMainLanguage: false,
    requiresTranslation: true,
    flagIconCode: 'fi-gb',
  },
  {
    languageId: 3,
    languageCode: 'ru',
    languageName: 'Русский',
    isMainLanguage: false,
    requiresTranslation: false,
    flagIconCode: 'fi-ru',
  },
]

const locations = [
  { locationId: 1, locationName: 'BCS Koolitus' },
  { locationId: 2, locationName: 'Veebiõpe' },
  { locationId: 3, locationName: 'Hübriidõpe' },
]

const lecturers = [
  { lecturerId: 1, lecturerName: 'Rain Tüür' },
  { lecturerId: 2, lecturerName: 'Merje Vaide' },
]

const trainings = [
  {
    trainingId: 1,
    categoryId: 1,
    trainingLanguageId: 1,
    locationId: 1,
    lecturerIds: [1],
    isOrderable: true,
    isPromoted: true,
    status: 'P',
    fundingTypeIds: [1],
  },
  {
    trainingId: 2,
    categoryId: 3,
    trainingLanguageId: 1,
    locationId: 2,
    lecturerIds: [2],
    isOrderable: false,
    isPromoted: false,
    status: 'P',
    fundingTypeIds: [],
  },
]

const trainingTranslations = [
  {
    trainingTranslationId: 1,
    trainingId: 1,
    languageId: 1,
    title: 'Java algkursus',
    shortDescription: 'Java programmeerimise alused algajatele.',
    // Vormindatud (richtext editori) kirjeldus; koolitus 2 jääb lihttekstiks nagu 3_import.sql-is
    description:
      '<p>Kursusel õpitakse <strong>Java süntaksit</strong>, <em>objektorienteeritud programmeerimist</em> ja põhilisi andmestruktuure.</p>' +
      '<h4>Mida sa õpid?</h4>' +
      '<ul><li><p>Muutujad, tingimuslaused ja tsüklid</p></li>' +
      '<li><p>Klassid ja objektid</p><ul><li><p>pärilus</p></li><li><p>liidesed</p></li></ul></li>' +
      '<li><p>Kollektsioonid: <strong>List</strong>, <strong>Set</strong>, <strong>Map</strong></p></li></ul>' +
      '<p>Loe lisaks <a target="_blank" rel="noopener noreferrer nofollow" href="https://dev.java/learn/">Java ametlikust õppematerjalist</a>.</p>',
  },
  {
    trainingTranslationId: 2,
    trainingId: 1,
    languageId: 2,
    title: 'Java Basics',
    shortDescription: 'Fundamentals of Java programming for beginners.',
    description:
      'The course covers Java syntax, object-oriented programming, and basic data structures.',
  },
  {
    trainingTranslationId: 3,
    trainingId: 2,
    languageId: 1,
    title: 'Projektijuhtimise põhitõed',
    shortDescription: 'Sissejuhatus IT-projektijuhtimisse.',
    description: 'Kursusel käsitletakse Scrumi, Kanbani ja projekti planeerimise põhimõtteid.',
  },
  {
    trainingTranslationId: 4,
    trainingId: 2,
    languageId: 2,
    title: 'Project Management Fundamentals',
    shortDescription: 'An introduction to IT project management.',
    description: 'The course covers Scrum, Kanban, and the principles of project planning.',
  },
]

let nextTrainingId = 3
let nextTrainingTranslationId = 5

function findLanguage(languageId) {
  return languages.find((language) => language.languageId === languageId)
}

function findTraining(trainingId) {
  return trainings.find((training) => training.trainingId === trainingId)
}

function findTrainingTranslation(trainingTranslationId) {
  return trainingTranslations.find(
    (translation) => translation.trainingTranslationId === trainingTranslationId,
  )
}

function mainLanguage() {
  return languages.find((language) => language.isMainLanguage)
}

export default {
  // GET /api/languages → SystemLanguageDto[]
  getLanguages() {
    return languages.map((language) => ({
      languageId: language.languageId,
      languageCode: language.languageCode,
      languageName: language.languageName,
      isMainLanguage: language.isMainLanguage,
      requiresTranslation: language.requiresTranslation,
      flagIconCode: language.flagIconCode,
    }))
  },

  // GET /api/locations → LocationDto[]
  getLocations() {
    return locations
  },

  // GET /api/lecturers?search= → LecturerDto[] (nime järgi tähestikuliselt)
  getLecturers(search) {
    const searchText = (search ?? '').toLowerCase()
    return lecturers
      .filter((lecturer) => lecturer.lecturerName.toLowerCase().includes(searchText))
      .sort((a, b) => a.lecturerName.localeCompare(b.lecturerName))
  },

  // GET /api/training/{trainingId} → TrainingDto (lecturers lecturerIds järjekorras)
  getTraining(trainingId) {
    const { lecturerIds, ...training } = findTraining(trainingId)
    return {
      ...training,
      lecturers: lecturerIds.map((lecturerId) =>
        lecturers.find((l) => l.lecturerId === lecturerId),
      ),
    }
  },

  // GET /api/training/{trainingId}/training-translations → TrainingTranslationItemDto[]
  getTrainingTranslations(trainingId) {
    return trainingTranslations
      .filter((translation) => translation.trainingId === trainingId)
      .map((translation) => ({
        trainingTranslationId: translation.trainingTranslationId,
        languageId: translation.languageId,
        languageCode: findLanguage(translation.languageId).languageCode,
        isMainLanguage: findLanguage(translation.languageId).isMainLanguage,
      }))
  },

  // GET /api/training-translation/{trainingTranslationId} → TrainingTranslationDto
  getTrainingTranslation(trainingTranslationId) {
    const translation = findTrainingTranslation(trainingTranslationId)
    return { ...translation, languageCode: findLanguage(translation.languageId).languageCode }
  },

  // POST /api/training → TrainingCreateResponseDto
  addTraining(trainingCreateRequest) {
    const trainingId = nextTrainingId++
    const trainingTranslationId = nextTrainingTranslationId++
    trainings.push({
      trainingId: trainingId,
      categoryId: trainingCreateRequest.categoryId,
      trainingLanguageId: trainingCreateRequest.trainingLanguageId,
      locationId: trainingCreateRequest.locationId,
      lecturerIds: [...trainingCreateRequest.lecturerIds],
      isOrderable: trainingCreateRequest.isOrderable,
      isPromoted: trainingCreateRequest.isPromoted,
      status: 'U',
      fundingTypeIds: [...new Set(trainingCreateRequest.fundingTypeIds)],
    })
    trainingTranslations.push({
      trainingTranslationId: trainingTranslationId,
      trainingId: trainingId,
      languageId: mainLanguage().languageId,
      title: trainingCreateRequest.title,
      shortDescription: trainingCreateRequest.shortDescription,
      description: trainingCreateRequest.description,
    })
    return { trainingId: trainingId, trainingTranslationId: trainingTranslationId }
  },

  // PUT /api/training/{trainingId} → NONE
  updateTraining(trainingId, trainingUpdateRequest) {
    const training = findTraining(trainingId)
    training.categoryId = trainingUpdateRequest.categoryId
    training.trainingLanguageId = trainingUpdateRequest.trainingLanguageId
    training.locationId = trainingUpdateRequest.locationId
    training.lecturerIds = [...trainingUpdateRequest.lecturerIds]
    training.isOrderable = trainingUpdateRequest.isOrderable
    training.isPromoted = trainingUpdateRequest.isPromoted
    training.fundingTypeIds = [...new Set(trainingUpdateRequest.fundingTypeIds)]
    const translation = findTrainingTranslation(trainingUpdateRequest.trainingTranslationId)
    translation.title = trainingUpdateRequest.title
    translation.shortDescription = trainingUpdateRequest.shortDescription
    translation.description = trainingUpdateRequest.description
  },

  // POST /api/training/{trainingId}/training-translation → TrainingTranslationCreateResponseDto
  addTrainingTranslation(trainingId, trainingTranslationCreateRequest) {
    const trainingTranslationId = nextTrainingTranslationId++
    trainingTranslations.push({
      trainingTranslationId: trainingTranslationId,
      trainingId: trainingId,
      languageId: trainingTranslationCreateRequest.languageId,
      title: trainingTranslationCreateRequest.title,
      shortDescription: trainingTranslationCreateRequest.shortDescription,
      description: trainingTranslationCreateRequest.description,
    })
    return { trainingTranslationId: trainingTranslationId }
  },

  // GET /api/training/{trainingId}/ai-translation?languageId= → AiTranslationDto
  // Päris teenus tõlgib AI abil; mock lisab põhikeele pealkirjale ja lühikirjeldusele keele eesliite.
  // description tagastatakse muutmata — HTML-i ette lisatud tekst jääks väljapoole <p>-d.
  getAiTranslation(trainingId, languageId) {
    const mainTranslation = trainingTranslations.find(
      (translation) =>
        translation.trainingId === trainingId &&
        translation.languageId === mainLanguage().languageId,
    )
    const prefix = '[AI ' + findLanguage(languageId).languageCode + '] '
    return {
      title: prefix + mainTranslation.title,
      shortDescription: prefix + mainTranslation.shortDescription,
      description: mainTranslation.description,
    }
  },
}
