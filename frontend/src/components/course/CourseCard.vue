<script>
import { mapState } from 'pinia'
import { PhCurrencyEur, PhPencilSimple, PhStar } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import FormatService from '@/services/FormatService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import FlagIcon from '@/components/common/FlagIcon.vue'

// Avaliku koolituste kalendri toimumiskorra kaart (CoursesView). Esile tõstetud: täht ja kollakas taust.
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
  <div class="card mb-3" :class="{ 'bg-warning-subtle': course.isPromoted }">
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-3 col-lg-2 text-center border-end">
          <div class="fw-bold fs-5">{{ courseDates }}</div>
          <div class="text-secondary small">
            {{
              $t('courses.duration', {
                days: course.numberOfDays,
                hours: course.numberOfAcademicHours,
              })
            }}
          </div>
        </div>
        <div class="col-md-6 col-lg-7">
          <h2 class="h5 d-flex align-items-center gap-2 mb-1">
            <PhStar
              v-if="course.isPromoted"
              :size="20"
              weight="fill"
              class="text-warning"
              :aria-label="$t('courses.promoted')"
            />
            {{ course.title }}
            <RouterLink
              v-if="userIsAdmin"
              :to="{ name: 'courseFormRoute', query: { courseId: course.courseId } }"
              :title="$t('courses.editCourse')"
              :aria-label="$t('courses.editCourse')"
              class="btn btn-sm btn-outline-secondary d-inline-flex ms-auto"
            >
              <PhPencilSimple :size="18" />
            </RouterLink>
          </h2>
          <p class="mb-2">{{ course.shortDescription }}</p>
          <div class="d-flex flex-wrap align-items-center gap-2 small">
            <span v-if="course.categoryName" class="badge text-bg-primary">
              {{ course.categoryName }}
            </span>
            <span v-if="course.isOnSite" class="badge text-bg-light border">
              {{ $t('courses.onSite') }}
            </span>
            <span v-if="course.isOnline" class="badge text-bg-light border">
              {{ $t('courses.online') }}
            </span>
            <span
              v-if="fundingTypeNames"
              class="d-inline-flex align-items-center gap-1 text-success"
            >
              <PhCurrencyEur :size="14" weight="bold" />
              {{ fundingTypeNames }}
            </span>
          </div>
          <div v-if="course.lecturerNames" class="small text-secondary mt-2">
            {{ $t('courses.lecturer') }}: {{ course.lecturerNames }}
          </div>
        </div>
        <div class="col-md-3 d-flex flex-column align-items-md-end justify-content-between gap-2">
          <div class="d-flex align-items-center gap-2">
            <FlagIcon
              :flag-icon-code="course.trainingLanguageFlagIconCode"
              :title="$t('trainingCard.language')"
              class="fs-4"
            />
            <span class="fs-5 fw-semibold text-nowrap">{{ price }} €</span>
          </div>
          <span v-if="course.status === 'F'" class="badge text-bg-warning">
            {{ $t('courseStatus.F') }}
          </span>
          <RouterLink
            :to="{ name: 'courseRoute', query: { courseId: course.courseId } }"
            class="btn btn-primary"
          >
            {{ $t('trainingCard.viewDetails') }}
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>
