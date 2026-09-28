<script>
export default {
  name: 'AlertSuccess',
  props: {
    successMessage: String,
  },
}
</script>

<template>
  <div>
    <div v-if="successMessage !== ''" class="alert alert-success" role="alert">
      {{ successMessage }}
    </div>
  </div>
</template>
