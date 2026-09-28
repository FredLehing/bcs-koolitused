<script>
export default {
  name: 'AlertDanger',
  props: {
    errorMessage: String,
  },
}
</script>

<template>
  <div>
    <div v-if="errorMessage !== ''" class="alert alert-danger" role="alert">{{ errorMessage }}</div>
  </div>
</template>
