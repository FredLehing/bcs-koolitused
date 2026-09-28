<template>
  <div class="d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-light bg-white mb-3">
      <div class="container">
        <RouterLink class="navbar-brand" to="/">
          <img src="@/assets/bcs-koolitus.svg" alt="BCS koolituse logo" height="68" />
        </RouterLink>
        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navMenu"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navMenu">
          <div class="navbar-nav gap-4 mx-auto bg-primary bg-opacity-50 rounded-pill px-3">
            <RouterLink class="nav-link" to="/trainings">Koolitused</RouterLink>
            <a class="nav-link" href="#">Teenused</a>

            <div class="dropdown">
              <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown"
                >Ettevõttest
              </a>
              <div class="dropdown-menu bg-primary bg-opacity-50">
                <a class="nav-link" href="#">Lektorid</a>
              </div>
            </div>

            <a class="nav-link" href="#">Blogi</a>
            <a class="nav-link" href="#">Kontakt</a>
            <a class="nav-link" href="#">Tagasiside</a>

            <div v-if="userIsAdmin" class="dropdown">
              <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">Admin </a>
              <div class="dropdown-menu bg-primary bg-opacity-50">
                <RouterLink class="nav-link" :to="{ name: 'trainingFormRoute' }"
                  >Lisa uus koolitus</RouterLink
                >
              </div>
            </div>
          </div>

          <div>
            <RouterLink class="btn btn-outline-secondary btn-sm me-3" to="/login"
              >Logi sisse</RouterLink
            >
            <a class="btn btn-outline-secondary btn-sm" href="#">Registreeri</a>
          </div>
        </div>
      </div>
    </nav>

    <RouterView />

    <FooterComponent />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import FooterComponent from '@/components/FooterComponent.vue'
import SessionStorageService from '@/services/SessionStorageService.js'

const route = useRoute()

// sessionStorage ei ole reaktiivne — kontroll tehakse uuesti iga marsruudi muutusel (nt pärast sisselogimist)
const userIsAdmin = computed(() => route.fullPath !== '' && SessionStorageService.userIsAdmin())
</script>
