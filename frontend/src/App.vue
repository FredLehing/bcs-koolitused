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
          <div>
            <a class="dropdown" href="#">Ettevõttest</a>
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

    <footer class="mt-auto">
      <div class="container">
        <div class="row">
          <div class="col-6">
            <div><PhFacebookLogo /> Leia meid Facebookist</div>
            <div><PhMapPin /> BCS Koolitus AS, Aia 7, Tallinn</div>
          </div>

          <div class="col-6 text-end">
            <div>Privaatsus-ja kasutustingimused</div>
            <div>Õppekorraldus-ja ostutingimused</div>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { PhFacebookLogo, PhMapPin } from '@phosphor-icons/vue'
</script>
