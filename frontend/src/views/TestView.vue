<template>
  <div class="container text-center">
    <div class="alert alert-danger" role="alert">
      See on "test" vaade, mis kutsutakse esile rajaga (/test)
    </div>
    <TestComponent />

    <p class="mb-4">
      Kogu see vaade koosneb erinevatest failidest, kus ühe faili sisu on teise sees, natukene nagu
      Matjoškad
    </p>
    <img
      class="mb-4"
      src="https://i.pinimg.com/originals/a6/9d/18/a69d18f3d91738c60be155208a492def.gif"
      alt="gif"
    />
    <TestComponent />
  </div>
</template>

<script>
import TestComponent from '@/components/TestComponent.vue'

export default {
  name: 'TestView',
  components: { TestComponent },
}
</script>
