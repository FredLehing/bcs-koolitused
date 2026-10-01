<script>
import { mapState } from 'pinia'
import { PhCurrencyEur, PhPencilSimple, PhStar } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import FormatService from '@/services/FormatService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import FlagIcon from '@/components/common/FlagIcon.vue'

// Avaliku koolituste kalendri toimumiskorra kaart (CoursesView). Esile tõstetud: märgis ja helesinine taust.
export default {
  name: 'CourseCard',
  components: { FlagIcon, PhCurrencyEur, PhPencilSimple, PhStar },
  props: {
    course: Object,
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    courseDates() {
      return FormatService.formatCourseDates(
        this.course.startDate,
        this.course.endDate,
        this.contentLang,
      )
    },

    fundingTypeNames() {
      return (this.course.fundingTypes ?? [])
        .map((fundingType) => fundingType.fundingTypeName)
        .join(', ')
    },

    price() {
      return FormatService.formatPrice(this.course.price, this.contentLang)
    },

    userIsAdmin() {
      return SessionStorageService.userIsAdmin()
    },
  },
}
</script>

<template>
  <article
    :class="course.isPromoted ? 'border-brand-200 bg-brand-50' : 'border-line bg-white'"
    class="flex flex-col gap-4 rounded-2xl border p-4 transition-shadow hover:shadow-lg hover:shadow-brand-600/10 sm:p-5 md:flex-row md:items-center md:gap-6"
  >
    <!-- Kuupäevaplokk -->
    <div
      class="flex items-center gap-3 md:w-36 md:shrink-0 md:flex-col md:items-stretch md:gap-1 md:rounded-xl md:border md:border-line md:bg-white md:px-3 md:py-3 md:text-center"
    >
      <div class="font-display text-lg leading-tight font-extrabold text-navy">
        {{ courseDates }}
      </div>
      <div class="text-sm text-muted">
        {{
          $t('courses.duration', {
            days: course.numberOfDays,
            hours: course.numberOfAcademicHours,
          })
        }}
      </div>
    </div>

    <div class="flex min-w-0 flex-1 flex-col gap-2">
      <div class="flex flex-wrap items-center gap-2">
        <h2 class="text-lg font-bold sm:text-xl">{{ course.title }}</h2>
        <span v-if="course.isPromoted" class="badge text-bg-primary">
          <PhStar :size="13" weight="fill" />
          {{ $t('courses.promoted') }}
        </span>
        <span v-if="course.status === 'F'" class="badge text-bg-warning">
          {{ $t('courseStatus.F') }}
        </span>
        <RouterLink
          v-if="userIsAdmin"
          :to="{
            name: 'courseFormRoute',
            query: { returnTo: $route.fullPath, courseId: course.courseId },
          }"
          :title="$t('courses.editCourse')"
          :aria-label="$t('courses.editCourse')"
          class="btn btn-outline-secondary btn-sm btn-icon"
        >
          <PhPencilSimple :size="18" />
        </RouterLink>
      </div>
      <p class="text-muted">{{ course.shortDescription }}</p>
      <div class="flex flex-wrap items-center gap-2 text-sm">
        <span
          v-if="course.categoryName"
          class="rounded-md bg-brand-100 px-2.5 py-0.5 font-semibold text-brand-700"
        >
          {{ course.categoryName }}
        </span>
        <span v-if="course.isOnSite" class="rounded-md bg-slate-100 px-2.5 py-0.5 text-slate-700">
          {{ $t('courses.onSite') }}
        </span>
        <span v-if="course.isOnline" class="rounded-md bg-slate-100 px-2.5 py-0.5 text-slate-700">
          {{ $t('courses.online') }}
        </span>
        <span
          v-if="fundingTypeNames"
          class="inline-flex items-center gap-1 font-medium text-emerald-800"
        >
          <PhCurrencyEur :size="14" weight="bold" />
          {{ fundingTypeNames }}
        </span>
      </div>
      <div v-if="course.lecturerNames" class="text-sm text-muted">
        {{ $t('courses.lecturer') }}: {{ course.lecturerNames }}
      </div>
    </div>

    <div
      class="flex items-center justify-between gap-4 border-t border-line pt-4 md:shrink-0 md:flex-col md:items-end md:border-0 md:pt-0"
    >
      <div class="flex items-center gap-3">
        <FlagIcon
          :flag-icon-code="course.trainingLanguageFlagIconCode"
          :title="$t('trainingCard.language')"
          class="text-xl"
        />
        <span class="font-display text-xl font-extrabold whitespace-nowrap text-navy">
          {{ price }} €
        </span>
      </div>
      <RouterLink
        :to="{
          name: 'courseRoute',
          query: { returnTo: $route.fullPath, courseId: course.courseId },
        }"
        class="btn btn-primary"
      >
        {{ $t('trainingCard.viewDetails') }}
      </RouterLink>
    </div>
  </article>
</template>
