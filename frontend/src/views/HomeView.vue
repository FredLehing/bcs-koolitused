<script>
import { PhX } from '@phosphor-icons/vue'
import NavigationService from '@/services/NavigationService.js'
import HomeGallery from '@/components/home/HomeGallery.vue'
import HomeTestimonials from '@/components/home/HomeTestimonials.vue'

export default {
  name: 'HomeView',
  components: { PhX, HomeGallery, HomeTestimonials },

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
    <section class="py-5 text-center">
      <h1 class="display-5 fw-bold">{{ $t('homeView.title') }}</h1>
      <p class="lead fw-normal text-body">{{ $t('homeView.subtitle') }}</p>
      <div class="col-lg-8 mx-auto">
        <div class="d-flex align-items-center gap-2 mb-3">
          <div class="input-group input-group-lg">
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
    </section>

    <HomeGallery />

    <HomeTestimonials />
  </div>
</template>
