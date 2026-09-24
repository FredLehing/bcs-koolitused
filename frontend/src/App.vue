<template>
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
      <div class="navbar-nav mx-auto">
        <a class="nav-link" href="#">Koolitused</a>
        <a class="nav-link" href="#">Teenused</a>

        <a class="nav-link" href="#">Ettevõttest</a>
        <a class="nav-link" href="#">Blogi</a>
        <a class="nav-link" href="#">Kontakt</a>
        <a class="nav-link" href="#">Tagasiside</a>
      </div>

      <div>
        <a class="btn btn-outline-secondary btn-sm me-3" href="#">Võta ühendust</a>
        <RouterLink class="btn btn-outline-secondary btn-sm me-3" to="/login">Logi sisse</RouterLink>
        <a class="btn btn-outline-secondary btn-sm" href="#">Registreeri</a>
      </div>
    </div>
  </nav>
  <RouterView />
</template>

<script setup></script>
