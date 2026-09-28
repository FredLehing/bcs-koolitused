<script>
export default {
  name: 'LanguagesDropdown',
  props: {
    languageId: {
      type: Number,
      default: 0,
    },
    languages: Array,
    firstOptionLabel: {
      type: String,
      default: '-- Vali keel --',
    },
    isDisabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-new-language-selected'],
}
</script>

<template>
  <select
    :value="languageId"
    @change="$emit('event-new-language-selected', Number($event.target.value))"
    :disabled="isDisabled"
    class="form-select"
  >
    <option :value="0">{{ firstOptionLabel }}</option>
    <option v-for="language in languages" :key="language.languageId" :value="language.languageId">
      {{ language.languageName }}
    </option>
  </select>
</template>
