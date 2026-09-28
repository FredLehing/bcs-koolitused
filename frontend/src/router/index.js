import { createRouter, createWebHistory } from 'vue-router'
import HomeView from "@/views/HomeView.vue";
import TestView from "@/views/TestView.vue";
import LoginView from "@/views/LoginView.vue";
import TrainingsView from '@/views/TrainingsView.vue'
import ErrorView from '@/views/ErrorView.vue'
import TrainingFormView from '@/views/TrainingFormView.vue'
import NotAuthorizedView from '@/views/NotAuthorizedView.vue'
import TrainingView from '@/views/TrainingView.vue'


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
      path: '/not-authorized',
      name: 'notAuthorizedRoute',
      component: NotAuthorizedView,
    },
    {
      path: '/error',
      name: 'errorRoute',
      component: ErrorView,
    },
  ]
})

export default router
