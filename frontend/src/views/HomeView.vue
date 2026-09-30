<script>
import { PhX } from '@phosphor-icons/vue'
import NavigationService from '@/services/NavigationService.js'

export default {
  name: 'HomeView',
  components: { PhX },

  data() {
    return {
      searchText: '',
    }
  },
  methods: {
    handleSearchClick() {
      const appliedSearchText = this.searchText.trim()
      if (appliedSearchText === '') {
        NavigationService.navigateToTrainingsView()
      } else {
        NavigationService.navigateToTrainingsView({ searchText: appliedSearchText })
      }
    },
    handleClearSearch() {
      this.searchText = ''
      this.$refs.searchInput.focus()
    },
  },
}
</script>

<template>
  <div class="container">
    <div class="d-flex align-items-center gap-2 mb-3">
      <div class="input-group">
        <input
          ref="searchInput"
          v-model="searchText"
          type="text"
          class="form-control"
          :placeholder="$t('trainings.searchPlaceholder')"
          @keyup.enter="handleSearchClick"
          @keyup.esc="handleClearSearch"
        />
        <button
          v-if="searchText"
          type="button"
          class="btn btn-outline-secondary"
          :title="$t('trainings.clearSearch')"
          :aria-label="$t('trainings.clearSearch')"
          @click="handleClearSearch"
        >
          <PhX :size="16" />
        </button>
        <button type="button" class="btn btn-primary" @click="handleSearchClick">
          {{ $t('trainings.search') }}
        </button>
      </div>
    </div>
  </div>
</template>
