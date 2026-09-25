<script>
export default {
name: "TrainingCard",
  props: {
  training: Object,
  },
}
</script>

<template>
  <div>{{training.title}}</div>
</template>
