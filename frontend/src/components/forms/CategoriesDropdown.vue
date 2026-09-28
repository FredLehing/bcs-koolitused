<script>
export default {
  name: 'CategoriesDropdown',
  props: {
    categoryId: {
      type: Number,
      default: 0,
    },
    categories: Array,
    firstOptionLabel: {
      type: String,
      default: '-- Vali kategooria --',
    },
    isDisabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-new-category-selected'],
}
</script>

<template>
  <select
    :value="categoryId"
    @change="$emit('event-new-category-selected', Number($event.target.value))"
    :disabled="isDisabled"
    class="form-select"
  >
    <option :value="0">{{ firstOptionLabel }}</option>
    <option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">
      {{ category.categoryName }}
    </option>
  </select>
</template>
