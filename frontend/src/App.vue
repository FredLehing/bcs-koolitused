<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-blue px-3 mb-3">
    <img src="@/assets/bcs-koolitus.svg" alt="svg" height="68" class="navbar-brand" to="/" />
    <button
      class="navbar-toggler"
      type="button"
      data-bs-toggle="collapse"
      data-bs-target="#navMenu"
    >
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse justify-content-center" id="navMenu">
      <div class="navbar-nav">
        <RouterLink class="nav-link" to="/">Home</RouterLink>
        <RouterLink class="nav-link" to="/test">Test</RouterLink>
        <RouterLink class="nav-link" to="/login">Logi Sisse</RouterLink>
      </div>
    </div>
  </nav>

  <RouterView />
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>
