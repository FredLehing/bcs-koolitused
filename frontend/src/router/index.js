import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import TestView from '@/views/TestView.vue'
import LoginView from '@/views/LoginView.vue'
import TrainingsView from '@/views/TrainingsView.vue'
import ErrorView from '@/views/ErrorView.vue'
import TrainingFormView from '@/views/TrainingFormView.vue'
import NotAuthorizedView from '@/views/NotAuthorizedView.vue'
import TrainingView from '@/views/TrainingView.vue'
import AdminTrainingsView from '@/views/AdminTrainingsView.vue'
import AdminLecturersView from '@/views/AdminLecturersView.vue'
import LecturerFormView from '@/views/LecturerFormView.vue'
import AdminRoomsView from '@/views/AdminRoomsView.vue'
import RoomFormView from '@/views/RoomFormView.vue'
import AdminEnquiriesView from '@/views/AdminEnquiriesView.vue'
import AdminEnquiryView from '@/views/AdminEnquiryView.vue'
import AdminTrainingCoursesView from '@/views/AdminTrainingCoursesView.vue'
import CourseFormView from '@/views/CourseFormView.vue'
import LecturersView from '@/views/LecturersView.vue'
import LecturerView from '@/views/LecturerView.vue'
import AdminAllCoursesView from '@/views/AdminAllCoursesView.vue'
import AdminCourseView from '@/views/AdminCourseView.vue'
import CoursesView from '@/views/CoursesView.vue'
import CourseView from '@/views/CourseView.vue'
import CourseRegistrationView from '@/views/CourseRegistrationView.vue'
import SignupView from '@/views/SignupView.vue'
import SessionStorageService from '@/services/SessionStorageService.js'

// Registreerumine: sisse logimata → login (pärast tagasi siia), admin ei registreeru
function checkParticipantUser(to) {
  if (!SessionStorageService.userIsLoggedIn()) {
    return { name: 'loginRoute', query: { redirect: to.fullPath } }
  }
  if (SessionStorageService.userIsAdmin()) {
    return { name: 'notAuthorizedRoute' }
  }
  return true
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'homeRoute',
      component: HomeView,
    },
    {
      path: '/test',
      name: 'testRoute',
      component: TestView,
    },
    {
      path: '/login',
      name: 'loginRoute',
      component: LoginView,
    },
    {
      path: '/trainings',
      name: 'trainingsRoute',
      component: TrainingsView,
    },
    {
      path: '/courses',
      name: 'coursesRoute',
      component: CoursesView,
    },
    {
      path: '/course',
      name: 'courseRoute',
      component: CourseView,
    },
    {
      path: '/course-registration',
      name: 'courseRegistrationRoute',
      component: CourseRegistrationView,
      beforeEnter: checkParticipantUser,
    },
    {
      path: '/signup',
      name: 'signupRoute',
      component: SignupView,
    },
    {
      path: '/training',
      name: 'trainingRoute',
      component: TrainingView,
    },
    {
      path: '/training-form',
      name: 'trainingFormRoute',
      component: TrainingFormView,
    },
    {
      path: '/admin-trainings',
      name: 'adminTrainingsRoute',
      component: AdminTrainingsView,
    },
    {
      path: '/admin-lecturers',
      name: 'adminLecturersRoute',
      component: AdminLecturersView,
    },
    {
      path: '/lecturer-form',
      name: 'lecturerFormRoute',
      component: LecturerFormView,
    },
    {
      path: '/admin-rooms',
      name: 'adminRoomsRoute',
      component: AdminRoomsView,
    },
    {
      path: '/room-form',
      name: 'roomFormRoute',
      component: RoomFormView,
    },
    {
      path: '/admin-enquiries',
      name: 'adminEnquiriesRoute',
      component: AdminEnquiriesView,
    },
    {
      path: '/admin-enquiry',
      name: 'adminEnquiryRoute',
      component: AdminEnquiryView,
    },
    {
      path: '/admin-training-courses',
      name: 'adminTrainingCoursesRoute',
      component: AdminTrainingCoursesView,
    },
    {
      path: '/course-form',
      name: 'courseFormRoute',
      component: CourseFormView,
    },
    {
      path: '/admin-all-courses',
      name: 'adminAllCoursesRoute',
      component: AdminAllCoursesView,
    },
    {
      path: '/admin-course',
      name: 'adminCourseRoute',
      component: AdminCourseView,
    },
    {
      path: '/lecturers',
      name: 'lecturersRoute',
      component: LecturersView,
    },
    {
      path: '/lecturer',
      name: 'lecturerRoute',
      component: LecturerView,
    },
    {
      path: '/not-authorized',
      name: 'notAuthorizedRoute',
      component: NotAuthorizedView,
    },
    {
      path: '/error',
      name: 'errorRoute',
      component: ErrorView,
    },
  ],
})

export default router
