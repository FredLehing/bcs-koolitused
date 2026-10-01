<script>
import { mapState } from 'pinia'
import { PhBooks, PhCalendarDots, PhX } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import CourseService from '@/api-services/CourseService.js'
import NavigationService from '@/services/NavigationService.js'
import CourseCard from '@/components/course/CourseCard.vue'
import HomeGallery from '@/components/home/HomeGallery.vue'
import HomeTestimonials from '@/components/home/HomeTestimonials.vue'

// Avalehe "Meie järgmised 5 koolitust" ploki toimumiskordade arv
const NEXT_COURSES_LIMIT = 5

export default {
  name: 'HomeView',
  components: { PhBooks, PhCalendarDots, PhX, CourseCard, HomeGallery, HomeTestimonials },

  data() {
    return {
      searchText: '',
      nextCourses: [],
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),
  },
  watch: {
    // Keele vahetus navbaris → toimumiskorrad uues keeles
    contentLang() {
      this.getNextCourses()
    },
  },
  methods: {
    // Tühi vastus või viga → plokk "Meie järgmised 5 koolitust" jääb peidetuks (veavaatesse ei suunata)
    getNextCourses() {
      CourseService.sendGetNextCoursesRequest(this.contentLang, NEXT_COURSES_LIMIT)
        .then((response) => (this.nextCourses = response.data))
        .catch(() => (this.nextCourses = []))
    },

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
  beforeMount() {
    this.getNextCourses()
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

    <!-- Suunavad kaardid: kogu kaart on link -->
    <section class="row g-4 mb-5">
      <div class="col-md-6">
        <RouterLink
          :to="{ name: 'trainingsRoute' }"
          class="card h-100 nav-card text-decoration-none"
        >
          <div class="card-body p-4 text-start">
            <PhBooks :size="48" class="text-primary mb-3" />
            <h2 class="h4 card-title text-body">{{ $t('homeView.trainingsCard.title') }}</h2>
            <p class="card-text text-secondary">{{ $t('homeView.trainingsCard.text') }}</p>
            <span class="link-primary fw-semibold">{{ $t('homeView.trainingsCard.link') }}</span>
          </div>
        </RouterLink>
      </div>
      <div class="col-md-6">
        <RouterLink :to="{ name: 'coursesRoute' }" class="card h-100 nav-card text-decoration-none">
          <div class="card-body p-4 text-start">
            <PhCalendarDots :size="48" class="text-primary mb-3" />
            <h2 class="h4 card-title text-body">{{ $t('homeView.coursesCard.title') }}</h2>
            <p class="card-text text-secondary">{{ $t('homeView.coursesCard.text') }}</p>
            <span class="link-primary fw-semibold">{{ $t('homeView.coursesCard.link') }}</span>
          </div>
        </RouterLink>
      </div>
    </section>

    <section v-if="nextCourses.length > 0" class="mb-5 text-start">
      <h2 class="h3 mb-3">{{ $t('homeView.nextCoursesTitle') }}</h2>
      <CourseCard
        v-for="nextCourse in nextCourses"
        :key="nextCourse.courseId"
        :course="nextCourse"
      />
    </section>

    <HomeTestimonials />

    <!-- Lõpu üleskutse pärast tagasisidet -->
    <section class="text-center border-top pt-4 mb-5">
      <p class="fs-5 mb-3">{{ $t('homeView.marketingText') }}</p>
      <RouterLink :to="{ name: 'coursesRoute' }" class="btn btn-primary btn-lg">
        {{ $t('homeView.allCoursesLink') }}
      </RouterLink>
    </section>
  </div>
</template>

<style scoped>
/* Suunav kaart: hover'il vari ja esiletõstetud ääris, et oleks näha, et see on link */
.nav-card {
  transition:
    box-shadow 0.15s ease-in-out,
    border-color 0.15s ease-in-out;
}

.nav-card:hover,
.nav-card:focus-visible {
  border-color: var(--bs-primary);
  box-shadow: var(--bs-box-shadow);
}
</style>
