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
import AdminTrainingCoursesView from '@/views/AdminTrainingCoursesView.vue'
import CourseFormView from '@/views/CourseFormView.vue'
import LecturersView from '@/views/LecturersView.vue'
import LecturerView from '@/views/LecturerView.vue'

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
