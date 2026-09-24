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
    <div class="collapse navbar-collapse justify-content-center" id="navMenu">
      <div class="navbar-nav">

        <a class="nav-link" href="#">Koolitused</a>
        <a class="nav-link" href="#">Teenused</a>
        <a class="nav-link" href="#">Ettevõttest</a>
        <a class="nav-link" href="#">Blogi</a>
        <a class="nav-link" href="#">Kontakt</a>
        <a class="nav-link" href="#">Tagasiside</a>
      </div>
    </div>
  </nav>
<RouterView />

</template>

<script setup>
</script>
