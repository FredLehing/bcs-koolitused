<script>
import { mapState } from 'pinia'
import { PhArrowRight, PhBooks, PhCalendarDots, PhMagnifyingGlass, PhX } from '@phosphor-icons/vue'
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
  components: {
    PhArrowRight,
    PhBooks,
    PhCalendarDots,
    PhMagnifyingGlass,
    PhX,
    CourseCard,
    HomeGallery,
    HomeTestimonials,
  },

  data() {
    return {
      searchText: '',
      nextCourses: [],
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    navCards() {
      return [
        { key: 'trainingsCard', routeName: 'trainingsRoute', icon: 'PhBooks' },
        { key: 'coursesCard', routeName: 'coursesRoute', icon: 'PhCalendarDots' },
      ]
    },
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
  <div>
    <!-- Lehe päis: pealkiri, otsing ja logo triibud -->
    <section class="overflow-hidden border-b border-brand-100 bg-brand-50">
      <div
        class="mx-auto flex w-full max-w-6xl items-center gap-12 px-4 py-14 sm:px-6 sm:py-20 lg:py-24"
      >
        <div class="flex flex-1 flex-col gap-5">
          <span class="font-display text-xs font-bold tracking-[0.12em] text-brand-600 uppercase">
            {{ $t('homeView.eyebrow') }}
          </span>
          <h1
            class="text-4xl leading-[1.08] font-extrabold tracking-tight sm:text-5xl lg:text-[56px]"
          >
            {{ $t('homeView.title') }}
          </h1>
          <p class="max-w-xl text-lg text-muted sm:text-xl">{{ $t('homeView.subtitle') }}</p>
          <form
            class="mt-2 flex max-w-xl flex-col gap-2 sm:flex-row"
            @submit.prevent="handleSearchClick"
          >
            <div
              class="flex min-h-14 flex-1 items-center gap-2 rounded-xl border border-brand-200 bg-white px-4 shadow-sm focus-within:border-brand-600 focus-within:ring-3 focus-within:ring-brand-600/15"
            >
              <PhMagnifyingGlass :size="20" class="shrink-0 text-muted" />
              <input
                ref="searchInput"
                v-model="searchText"
                type="text"
                class="min-w-0 flex-1 bg-transparent text-lg outline-none"
                :placeholder="$t('trainings.searchPlaceholder')"
                :aria-label="$t('trainings.searchPlaceholder')"
                @keyup.esc="handleClearSearch"
              />
              <button
                v-if="searchText"
                type="button"
                class="btn btn-link btn-sm text-muted"
                :title="$t('trainings.clearSearch')"
                :aria-label="$t('trainings.clearSearch')"
                @click="handleClearSearch"
              >
                <PhX :size="18" />
              </button>
            </div>
            <button type="submit" class="btn btn-primary btn-lg min-h-14">
              {{ $t('trainings.search') }}
            </button>
          </form>
        </div>
        <!-- Logo triibud kujunduselemendina (ainult laial ekraanil) -->
        <div class="hidden w-80 shrink-0 flex-col gap-5 lg:flex xl:w-96" aria-hidden="true">
          <div class="h-11 w-[92%] rounded-r-full bg-brand-600"></div>
          <div class="ml-[26%] h-11 w-[74%] rounded-full bg-brand-300"></div>
          <div class="h-11 w-full rounded-r-full bg-brand-600"></div>
          <div class="ml-[12%] h-11 w-[58%] rounded-full bg-brand-200"></div>
        </div>
      </div>
    </section>

    <div class="mx-auto flex w-full max-w-6xl flex-col gap-14 px-4 py-12 sm:px-6 sm:py-16">
      <HomeGallery />

      <!-- Suunavad kaardid: kogu kaart on link -->
      <section class="grid gap-5 md:grid-cols-2">
        <RouterLink
          v-for="navCard in navCards"
          :key="navCard.routeName"
          :to="{ name: navCard.routeName }"
          class="group flex flex-col gap-3 rounded-2xl border border-line bg-white p-6 text-ink transition hover:border-brand-300 hover:text-ink hover:shadow-xl hover:shadow-brand-600/10 sm:p-8"
        >
          <span
            class="flex size-13 items-center justify-center rounded-xl bg-brand-100 text-brand-600"
          >
            <component :is="navCard.icon" :size="28" />
          </span>
          <h2 class="text-2xl font-bold">{{ $t(`homeView.${navCard.key}.title`) }}</h2>
          <p class="text-muted">{{ $t(`homeView.${navCard.key}.text`) }}</p>
          <span
            class="mt-1 inline-flex items-center gap-1.5 font-semibold text-brand-600 group-hover:gap-2.5 transition-all"
          >
            {{ $t(`homeView.${navCard.key}.link`).replace('→', '').trim() }}
            <PhArrowRight :size="18" weight="bold" />
          </span>
        </RouterLink>
      </section>

      <section v-if="nextCourses.length > 0">
        <div class="mb-6 flex flex-wrap items-end justify-between gap-3">
          <h2 class="text-2xl font-extrabold sm:text-3xl">{{ $t('homeView.nextCoursesTitle') }}</h2>
          <RouterLink :to="{ name: 'coursesRoute' }" class="font-semibold">
            {{ $t('navbar.coursesCalendar') }} →
          </RouterLink>
        </div>
        <div class="flex flex-col gap-4">
          <CourseCard
            v-for="nextCourse in nextCourses"
            :key="nextCourse.courseId"
            :course="nextCourse"
          />
        </div>
      </section>

      <HomeTestimonials />

      <!-- Lõpu üleskutse pärast tagasisidet -->
      <section
        class="flex flex-col items-start gap-6 rounded-3xl bg-navy p-8 text-white sm:p-12 md:flex-row md:items-center md:justify-between"
      >
        <p class="max-w-xl font-display text-2xl leading-snug font-bold sm:text-3xl">
          {{ $t('homeView.marketingText') }}
        </p>
        <RouterLink :to="{ name: 'coursesRoute' }" class="btn btn-white btn-lg">
          {{ $t('homeView.allCoursesLink') }}
        </RouterLink>
      </section>
    </div>
  </div>
</template>
