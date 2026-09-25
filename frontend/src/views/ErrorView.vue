<script>
import NavigationService from "@/services/NavigationService.js";

export default {
  name: 'ErrorView',

  methods: {
    pushToTrainingsView() {
      NavigationService.navigateToTrainingsView()
    }
  },
}
</script>

<template>
  <div class="container text-center">
    <p>Midagi läks valesti, proovi hiljem uuesti!</p>
    <button @click="pushToTrainingsView" type="button" class="btn btn-primary mt-3">Tagasi koolituste juurde</button>
  </div>
</template>
