<script>
import BackLink from '@/components/common/BackLink.vue'
import { mapState } from 'pinia'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import CourseService from '@/api-services/CourseService.js'
import RoomService from '@/api-services/RoomService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'
import CourseDeleteButton from '@/components/common/CourseDeleteButton.vue'
import DateInput from '@/components/forms/DateInput.vue'
import LecturersPicker from '@/components/forms/LecturersPicker.vue'
import RoomsDropdown from '@/components/forms/RoomsDropdown.vue'

// Vaate olekud tuletatakse URL-i query parameetritest:
//   new     /course-form?trainingId={id}
//   update  /course-form?courseId={id}
const STATE_NEW = 'new'
const STATE_UPDATE = 'update'

const COURSE_STATUSES = ['U', 'O', 'F', 'X']

// Tööpäevade (E–R) arv alguse ja lõpu vahel (mõlemad kaasa arvatud); 0, kui lõpp on enne algust
function countWorkingDays(startDate, endDate) {
  const current = new Date(`${startDate}T00:00:00`)
  const end = new Date(`${endDate}T00:00:00`)
  let workingDays = 0
  while (current <= end) {
    const weekDay = current.getDay()
    if (weekDay !== 0 && weekDay !== 6) {
      workingDays++
    }
    current.setDate(current.getDate() + 1)
  }
  return workingDays
}

export default {
  name: 'CourseFormView',
  components: {
    BackLink,
    AlertDanger,
    CourseDeleteButton,
    DateInput,
    LecturersPicker,
    RoomsDropdown,
  },
  data() {
    return {
      state: STATE_NEW,
      errorMessage: '',
      trainingId: 0,
      courseId: 0,
      trainingTitle: '',
      rooms: [],
      // Toimumiskorra praegune ruum ({ roomId, roomName }) — kustutatud ruumi GET /api/rooms ei tagasta
      linkedRoom: null,
      // true, kui admin on päevade arvu ise muutnud — siis seda kuupäevadest enam ei arvutata
      isNumberOfDaysEdited: false,
      isSending: false,
      course: {
        startDate: '',
        endDate: '',
        numberOfDays: null,
        numberOfAcademicHours: null,
        price: null,
        lecturers: [],
        roomId: null,
        status: 'U',
        isPromoted: false,
        notes: '',
        meetingLink: '',
      },
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    isNew() {
      return this.state === STATE_NEW
    },

    courseStatuses() {
      return COURSE_STATUSES
    },

    // Kustutatud praegune ruum jääb rippmenüüsse märgisega "(kustutatud)", et valik ei kaoks
    roomOptions() {
      const linkedRoom = this.linkedRoom
      if (linkedRoom === null || this.rooms.some((room) => room.roomId === linkedRoom.roomId)) {
        return this.rooms
      }
      const deletedRoom = {
        roomId: linkedRoom.roomId,
        roomName: `${linkedRoom.roomName} ${this.$t('courseForm.roomDeleted')}`,
      }
      return [...this.rooms, deletedRoom]
    },
  },
  watch: {
    '$route.query'() {
      this.loadView()
    },

    // Keele vahetus → koolituse nimi uues keeles (vormi väljad jäävad)
    contentLang() {
      if (this.trainingId !== 0) {
        this.getAdminTraining(false)
      }
    },

    'course.startDate'() {
      this.handleSuggestNumberOfDays()
    },

    'course.endDate'() {
      this.handleSuggestNumberOfDays()
    },
  },
  methods: {
    loadView() {
      this.trainingId = Number(this.$route.query.trainingId ?? 0)
      this.courseId = Number(this.$route.query.courseId ?? 0)
      this.state = this.courseId === 0 ? STATE_NEW : STATE_UPDATE
      this.errorMessage = ''
      this.isNumberOfDaysEdited = false
      this.linkedRoom = null
      this.getRooms()
      if (this.isNew) {
        this.resetCourse()
        if (this.trainingId === 0) {
          NavigationService.navigateToErrorView()
          return
        }
        this.getAdminTraining(true)
      } else {
        this.getCourse()
      }
    },

    getRooms() {
      RoomService.sendGetRoomsRequest()
        .then((response) => (this.rooms = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Uus toimumiskord eeltäidetakse koolituse koolitajatega (edasi on need koolitusest sõltumatud)
    getAdminTraining(isPrefillLecturers) {
      TrainingService.sendGetAdminTrainingRequest(this.trainingId, this.contentLang)
        .then((response) => this.handleGetAdminTrainingResponse(response.data, isPrefillLecturers))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetAdminTrainingResponse(adminTraining, isPrefillLecturers) {
      this.trainingTitle = adminTraining.title
      if (isPrefillLecturers) {
        this.course.lecturers = adminTraining.lecturers
      }
    },

    getCourse() {
      CourseService.sendGetCourseRequest(this.courseId)
        .then((response) => this.handleGetCourseResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // Salvestatud päevade arv on admini valik — kuupäevade muutmine seda üle ei kirjuta
    handleGetCourseResponse(courseDto) {
      this.isNumberOfDaysEdited = true
      this.trainingId = courseDto.trainingId
      this.linkedRoom =
        courseDto.roomId === null
          ? null
          : { roomId: courseDto.roomId, roomName: courseDto.roomName }
      this.course = {
        startDate: courseDto.startDate,
        endDate: courseDto.endDate,
        numberOfDays: courseDto.numberOfDays,
        numberOfAcademicHours: courseDto.numberOfAcademicHours,
        price: courseDto.price,
        lecturers: courseDto.lecturers,
        roomId: courseDto.roomId,
        status: courseDto.status,
        isPromoted: courseDto.isPromoted,
        notes: courseDto.notes ?? '',
        meetingLink: courseDto.meetingLink ?? '',
      }
      this.getAdminTraining(false)
    },

    handleSuggestNumberOfDays() {
      if (!this.isNumberOfDaysEdited && this.course.startDate && this.course.endDate) {
        const workingDays = countWorkingDays(this.course.startDate, this.course.endDate)
        this.course.numberOfDays = workingDays > 0 ? workingDays : null
      }
    },

    handleNumberOfDaysInput(value) {
      this.isNumberOfDaysEdited = true
      this.course.numberOfDays = value === '' ? null : Number(value)
    },

    // ---------- "Salvesta" ----------

    saveCourse() {
      this.errorMessage = ''
      this.checkCourseForErrors()
      if (this.errorMessage !== '') {
        return
      }
      this.isSending = true
      const request = this.isNew
        ? CourseService.sendPostCourseRequest(this.trainingId, {
            userId: SessionStorageService.getUserId(),
            ...this.createCourseRequest(),
          })
        : CourseService.sendPutCourseRequest(this.courseId, this.createCourseRequest())
      request
        .then(() => this.handleSaveCourseResponse())
        .catch((error) => this.handleSaveCourseError(error))
        .finally(() => (this.isSending = false))
    },

    createCourseRequest() {
      return {
        startDate: this.course.startDate,
        endDate: this.course.endDate,
        numberOfDays: this.course.numberOfDays,
        numberOfAcademicHours: this.course.numberOfAcademicHours,
        price: this.course.price,
        lecturerIds: this.course.lecturers.map((lecturer) => lecturer.lecturerId),
        roomId: this.course.roomId,
        status: this.course.status,
        isPromoted: this.course.isPromoted,
        notes: this.course.notes,
        meetingLink: this.course.meetingLink,
      }
    },

    handleSaveCourseResponse() {
      const successMessage = this.isNew
        ? this.$t('courseForm.messages.added')
        : this.$t('courseForm.messages.saved')
      NavigationService.navigateBack(
        { name: 'adminTrainingCoursesRoute', query: { trainingId: this.trainingId } },
        successMessage,
      )
    },

    // 403 COURSE_END_BEFORE_START, 400 ja vahepeal kustutatud koolitaja või ruum (404 'lecturerId' /
    // 'roomId') → teade vormis
    handleSaveCourseError(error) {
      const statusCode = error.response?.status
      const message = error.response?.data?.message ?? ''
      if (
        statusCode === 400 ||
        statusCode === 403 ||
        (statusCode === 404 && (message.includes("'lecturerId'") || message.includes("'roomId'")))
      ) {
        this.errorMessage = message
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    checkCourseForErrors() {
      if (
        !this.course.startDate ||
        !this.course.endDate ||
        !(this.course.numberOfDays >= 1) ||
        !(this.course.numberOfAcademicHours >= 1) ||
        this.course.price === null ||
        this.course.price === '' ||
        !(Number(this.course.price) >= 0) ||
        !this.course.status
      ) {
        this.errorMessage = this.$t('courseForm.validation.fillRequired')
      } else if (this.course.endDate < this.course.startDate) {
        this.errorMessage = this.$t('courseForm.validation.endBeforeStart')
      }
    },

    resetCourse() {
      this.course = {
        startDate: '',
        endDate: '',
        numberOfDays: null,
        numberOfAcademicHours: null,
        price: null,
        lecturers: [],
        roomId: null,
        status: 'U',
        isPromoted: false,
        notes: '',
        meetingLink: '',
      }
    },

    handleCourseDeleted() {
      NavigationService.navigateBack(
        { name: 'adminTrainingCoursesRoute', query: { trainingId: this.trainingId } },
        this.$t('adminTrainingCourses.messages.deleted'),
        ['adminCourseRoute', 'courseRoute', 'courseFormRoute'],
      )
    },

    navigateBack() {
      NavigationService.navigateBack({
        name: 'adminTrainingCoursesRoute',
        query: { trainingId: this.trainingId },
      })
    },

    navigateToAdminTrainingCoursesView() {
      NavigationService.navigateToAdminTrainingCoursesView(this.trainingId)
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.loadView()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
  },
}
</script>

<template>
  <div class="mx-auto w-full max-w-6xl px-6 py-8">
    <BackLink :fallback="{ name: 'adminTrainingCoursesRoute', query: { trainingId } }" />
    <div class="mb-1 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-3xl font-extrabold tracking-tight">
        {{ isNew ? $t('courseForm.title.new') : $t('courseForm.title.update') }}
      </h1>
      <button
        @click="navigateToAdminTrainingCoursesView"
        class="btn btn-outline-secondary"
        type="button"
      >
        {{ $t('trainingForm.buttons.calendar') }}
      </button>
    </div>
    <p class="mb-6 text-lg text-muted">{{ trainingTitle }}</p>

    <section class="rounded-2xl border border-line bg-white p-6" aria-labelledby="course-heading">
      <h2 id="course-heading" class="mb-4 text-lg font-bold">{{ $t('courseForm.legend') }}</h2>
      <div class="grid gap-4 md:grid-cols-3">
        <div>
          <label class="form-label" for="startDate">{{ $t('courseForm.startDate') }} *</label>
          <DateInput
            :date="course.startDate"
            input-id="startDate"
            @event-new-date-input="course.startDate = $event"
          />
        </div>
        <div>
          <label class="form-label" for="endDate">{{ $t('courseForm.endDate') }} *</label>
          <DateInput
            :date="course.endDate"
            input-id="endDate"
            @event-new-date-input="course.endDate = $event"
          />
        </div>
        <div>
          <label class="form-label" for="numberOfDays">{{ $t('courseForm.numberOfDays') }} *</label>
          <input
            :value="course.numberOfDays"
            @input="handleNumberOfDaysInput($event.target.value)"
            id="numberOfDays"
            class="form-control"
            type="number"
            min="1"
          />
          <div class="form-text">{{ $t('courseForm.numberOfDaysHint') }}</div>
        </div>
        <div>
          <label class="form-label" for="numberOfAcademicHours"
            >{{ $t('courseForm.numberOfAcademicHours') }} *</label
          >
          <input
            v-model.number="course.numberOfAcademicHours"
            id="numberOfAcademicHours"
            class="form-control"
            type="number"
            min="1"
          />
        </div>
        <div>
          <label class="form-label" for="price">{{ $t('courseForm.price') }} *</label>
          <input
            v-model="course.price"
            id="price"
            class="form-control"
            type="number"
            min="0"
            step="0.01"
          />
        </div>
        <div>
          <label class="form-label" for="status">{{ $t('courseForm.status') }} *</label>
          <select v-model="course.status" id="status" class="form-select">
            <option
              v-for="courseStatus in courseStatuses"
              :key="courseStatus"
              :value="courseStatus"
            >
              {{ $t(`courseStatus.${courseStatus}`) }}
            </option>
          </select>
          <div class="form-check form-switch mt-2">
            <input
              v-model="course.isPromoted"
              id="isPromoted"
              class="form-check-input"
              type="checkbox"
              role="switch"
            />
            <label class="form-check-label" for="isPromoted">{{
              $t('courseForm.isPromoted')
            }}</label>
          </div>
          <div class="form-text">{{ $t('courseForm.isPromotedHint') }}</div>
        </div>
      </div>

      <div class="mt-6 grid gap-4 border-t border-line pt-6 md:grid-cols-2">
        <div>
          <h3 class="form-label">{{ $t('trainingForm.data.lecturers') }}</h3>
          <LecturersPicker
            :lecturers="course.lecturers"
            @event-lecturers-changed="course.lecturers = $event"
          />
        </div>
        <div class="flex flex-col gap-4">
          <div>
            <label class="form-label" for="roomId">{{ $t('courseForm.room') }}</label>
            <!-- id läheb läbi komponendi <select>-ile -->
            <RoomsDropdown
              id="roomId"
              :room-id="course.roomId"
              :rooms="roomOptions"
              @event-new-room-selected="course.roomId = $event"
            />
          </div>
          <div>
            <label class="form-label" for="meetingLink">{{ $t('courseForm.meetingLink') }}</label>
            <input
              v-model="course.meetingLink"
              id="meetingLink"
              class="form-control"
              type="url"
              maxlength="255"
            />
          </div>
        </div>
        <div class="md:col-span-2">
          <label class="form-label" for="notes">{{ $t('courseForm.notes') }}</label>
          <textarea v-model="course.notes" id="notes" class="form-control" rows="3"></textarea>
        </div>
      </div>
    </section>

    <AlertDanger :error-message="errorMessage" class="mt-6" />

    <div class="mt-6 flex flex-wrap items-center gap-3">
      <button @click="saveCourse" :disabled="isSending" class="btn btn-primary" type="button">
        {{ $t('trainingForm.buttons.save') }}
      </button>
      <button @click="navigateBack" class="btn btn-outline-secondary" type="button">
        {{ $t('courseForm.back') }}
      </button>
      <!-- CourseDeleteButton'il on mitu juurelementi, seega paigutus ümbrisega -->
      <div v-if="!isNew" class="ml-auto">
        <CourseDeleteButton
          :course-id="courseId"
          :start-date="course.startDate"
          :end-date="course.endDate"
          @event-course-deleted="handleCourseDeleted"
        />
      </div>
    </div>
  </div>
</template>
