<template>
  <div class="container text-center">
    <div class="alert alert-primary" role="alert">See on koduvaade, see asub rajal (/)</div>
    <TestComponent/>
  </div>
</template>

<script>

import TestComponent from "@/components/TestComponent.vue";

export default {
  name: 'HomeView',
  components: {TestComponent},
}
</script>
