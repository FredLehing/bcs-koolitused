<template>
  <div class="d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-light bg-white px-3 mb-3">
      <img src="@/assets/bcs-koolitus.svg" alt="svg" height="68" class="navbar-brand" to="/" />
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
          <a class="nav-link" href="#">Koolitused</a>
          <a class="nav-link" href="#">Teenused</a>

          <div class="dropdown">
            <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">Ettevõttest </a>
            <div class="dropdown-menu bg-primary bg-opacity-50">
              <a class="nav-link" href="#">Lektorid</a>
            </div>
          </div>

          <a class="nav-link" href="#">Blogi</a>
          <a class="nav-link" href="#">Kontakt</a>
          <a class="nav-link" href="#">Tagasiside</a>
        </div>

        <div>
          <RouterLink class="btn btn-outline-secondary btn-sm me-3" to="/login"
            >Logi sisse</RouterLink
          >
          <a class="btn btn-outline-secondary btn-sm" href="#">Registreeri</a>
        </div>
      </div>
    </nav>

    <RouterView />

    <FooterComponent />
  </div>
</template>

<script setup>
import FooterComponent from '@/components/FooterComponent.vue'
</script>
