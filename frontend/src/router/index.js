import { createRouter, createWebHistory } from 'vue-router'
import HomeView from "@/views/HomeView.vue";
import TestView from "@/views/TestView.vue";
import LoginView from "@/views/LoginView.vue";
import TrainingsView from '@/views/TrainingsView.vue'


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
  ]
})

export default router
