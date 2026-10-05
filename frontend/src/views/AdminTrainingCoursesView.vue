<script>
import BackLink from '@/components/common/BackLink.vue'
import { mapState } from 'pinia'
import { PhPencilSimple, PhPlus } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import TrainingService from '@/api-services/TrainingService.js'
import CourseService from '@/api-services/CourseService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import FormatService from '@/services/FormatService.js'
import InlineAlerts from '@/components/common/InlineAlerts.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'
import CollapsibleCard from '@/components/common/CollapsibleCard.vue'
import RichTextContent from '@/components/common/RichTextContent.vue'
import CourseStatusBadge from '@/components/common/CourseStatusBadge.vue'
import CheckMark from '@/components/common/CheckMark.vue'
import CourseDeleteButton from '@/components/common/CourseDeleteButton.vue'
import TrainingSummaryCard from '@/components/training/TrainingSummaryCard.vue'

// Staatuse kasvav järjekord sorteerimisel: Mustand → Avatud → Täis → Tühistatud
const STATUS_ORDER = { U: 1, O: 2, F: 3, X: 4 }

// Sorteeritava veeru väärtus võrdlemiseks
const SORT_VALUES = {
  startDate: (course) => course.startDate,
  price: (course) => Number(course.price),
  status: (course) => STATUS_ORDER[course.status],
  participantCount: (course) => course.participantCount,
}

export default {
  name: 'AdminTrainingCoursesView',
  components: {
    BackLink,
    PhPencilSimple,
    PhPlus,
    InlineAlerts,
    SortableColumnHeader,
    CollapsibleCard,
    RichTextContent,
    CourseStatusBadge,
    CheckMark,
    CourseDeleteButton,
    TrainingSummaryCard,
  },
  data() {
    return {
      successMessage: '',
      errorMessage: '',
      trainingId: 0,
      training: null,
      courses: [],
      includePast: false,
      isDescriptionOpen: false,
      // null = backendi vaikimisi järjestus (tulevased lähimast, siis möödunud)
      sortBy: null,
      sortDirection: 'asc',
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),

    sortableColumns() {
      return {
        startDate: this.$t('adminTrainingCourses.columns.startDate'),
        price: this.$t('adminTrainingCourses.columns.price'),
        status: this.$t('adminTrainingCourses.columns.status'),
        participantCount: this.$t('adminTrainingCourses.columns.participantCount'),
      }
    },

    // Sorteerimine ainult frontendis; võrdsete väärtuste korral jääb vaikimisi järjestus (stabiilne sort)
    sortedCourses() {
      if (this.sortBy === null) {
        return this.courses
      }
      const sortValue = SORT_VALUES[this.sortBy]
      const direction = this.sortDirection === 'asc' ? 1 : -1
      return [...this.courses].sort((a, b) => {
        const valueA = sortValue(a)
        const valueB = sortValue(b)
        if (valueA === valueB) {
          return 0
        }
        return (valueA < valueB ? -1 : 1) * direction
      })
    },

    emptyTableText() {
      return this.includePast
        ? this.$t('adminTrainingCourses.noCourses')
        : this.$t('adminTrainingCourses.noUpcomingCourses')
    },
  },
  watch: {
    // Keele vahetus → ainult koolituse andmed (toimumiskorrad ei sõltu keelest)
    contentLang() {
      this.getAdminTraining()
    },

    includePast() {
      this.getCourses()
    },
  },
  methods: {
    loadView() {
      this.trainingId = Number(this.$route.query.trainingId ?? 0)
      if (this.trainingId === 0) {
        NavigationService.navigateToErrorView()
        return
      }
      this.successMessage = window.history.state?.successMessage ?? ''
      this.getAdminTraining()
      this.getCourses()
    },

    getAdminTraining() {
      TrainingService.sendGetAdminTrainingRequest(this.trainingId, this.contentLang)
        .then((response) => (this.training = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getCourses() {
      CourseService.sendGetTrainingCoursesRequest(this.trainingId, this.includePast)
        .then((response) => (this.courses = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // 1. klõps kasvav → 2. kahanev → 3. vaikimisi järjestus
    handleSortClick(sortKey) {
      if (this.sortBy !== sortKey) {
        this.sortBy = sortKey
        this.sortDirection = 'asc'
      } else if (this.sortDirection === 'asc') {
        this.sortDirection = 'desc'
      } else {
        this.sortBy = null
        this.sortDirection = 'asc'
      }
    },

    handleCourseDeleted() {
      this.errorMessage = ''
      this.successMessage = this.$t('adminTrainingCourses.messages.deleted')
      this.getCourses()
    },

    navigateToNewCourseForm() {
      NavigationService.navigateToCourseFormView({ trainingId: this.trainingId })
    },

    navigateToAdminTrainingsView() {
      NavigationService.navigateToAdminTrainingsView()
    },

    formatLocalDate(localDate) {
      return FormatService.formatLocalDate(localDate)
    },

    formatPrice(price) {
      return FormatService.formatPrice(price, this.contentLang)
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.loadView()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
  },
}
</script>

<template>
  <div class="container">
    <BackLink :fallback="{ name: 'adminTrainingsRoute' }" />
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
      <h1 class="h3 mb-0">{{ $t('adminTrainingCourses.title') }}</h1>
      <div class="d-flex flex-wrap gap-2">
        <button
          @click="navigateToAdminTrainingsView"
          class="btn btn-outline-secondary"
          type="button"
        >
          {{ $t('navbar.manageTrainings') }}
        </button>
        <button
          @click="navigateToNewCourseForm"
          class="btn btn-primary d-inline-flex align-items-center gap-1"
          type="button"
        >
          <PhPlus :size="18" />
          {{ $t('adminTrainingCourses.addCourse') }}
        </button>
      </div>
    </div>

    <template v-if="training">
      <TrainingSummaryCard :training="training">
        <template #actions>
          <RouterLink
            :to="{
              name: 'trainingRoute',
              query: {
                returnTo: $route.fullPath,
                trainingId: training.trainingId,
                trainingTranslationId: training.trainingTranslationId,
              },
            }"
            class="btn btn-sm btn-outline-secondary"
          >
            {{ $t('trainingForm.buttons.view') }}
          </RouterLink>
          <RouterLink
            :to="{
              name: 'trainingFormRoute',
              query: {
                returnTo: $route.fullPath,
                trainingId: training.trainingId,
                trainingTranslationId: training.trainingTranslationId,
              },
            }"
            class="btn btn-sm btn-outline-secondary"
          >
            {{ $t('trainingCard.edit') }}
          </RouterLink>
        </template>
      </TrainingSummaryCard>

      <CollapsibleCard
        :is-open="isDescriptionOpen"
        :show-label="$t('adminTrainingCourses.showDescription')"
        :hide-label="$t('adminTrainingCourses.hideDescription')"
        content-id="training-description"
        @event-toggle-clicked="isDescriptionOpen = !isDescriptionOpen"
      >
        <RichTextContent :html="training.description" />
      </CollapsibleCard>
    </template>

    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2">
      <div class="form-check form-switch mb-0">
        <input
          v-model="includePast"
          id="includePast"
          class="form-check-input"
          type="checkbox"
          role="switch"
        />
        <label class="form-check-label" for="includePast">
          {{ $t('adminTrainingCourses.includePast') }}
        </label>
      </div>
      <InlineAlerts
        :success-message="successMessage"
        :error-message="errorMessage"
        @event-success-message-closed="successMessage = ''"
        @event-error-message-closed="errorMessage = ''"
      />
    </div>

    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead>
          <tr>
            <SortableColumnHeader
              :label="sortableColumns.startDate"
              sort-key="startDate"
              :sort-by="sortBy"
              :sort-direction="sortDirection"
              @event-sort-clicked="handleSortClick"
            />
            <th>{{ $t('adminTrainingCourses.columns.endDate') }}</th>
            <th>{{ $t('adminTrainingCourses.columns.numberOfDays') }}</th>
            <th>{{ $t('adminTrainingCourses.columns.numberOfAcademicHours') }}</th>
            <SortableColumnHeader
              :label="sortableColumns.price"
              sort-key="price"
              :sort-by="sortBy"
              :sort-direction="sortDirection"
              @event-sort-clicked="handleSortClick"
            />
            <th>{{ $t('adminTrainingCourses.columns.lecturers') }}</th>
            <th>{{ $t('adminTrainingCourses.columns.room') }}</th>
            <SortableColumnHeader
              :label="sortableColumns.status"
              sort-key="status"
              :sort-by="sortBy"
              :sort-direction="sortDirection"
              @event-sort-clicked="handleSortClick"
            />
            <SortableColumnHeader
              :label="sortableColumns.participantCount"
              sort-key="participantCount"
              :sort-by="sortBy"
              :sort-direction="sortDirection"
              @event-sort-clicked="handleSortClick"
            />
            <th>{{ $t('adminTrainingCourses.columns.notes') }}</th>
            <th>{{ $t('adminTrainingCourses.columns.meetingLink') }}</th>
            <th>{{ $t('adminTrainingCourses.columns.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="course in sortedCourses"
            :key="course.courseId"
            :class="{ 'past-row': course.isPast }"
          >
            <td class="text-nowrap">{{ formatLocalDate(course.startDate) }}</td>
            <td class="text-nowrap">{{ formatLocalDate(course.endDate) }}</td>
            <td>{{ course.numberOfDays }}</td>
            <td>{{ course.numberOfAcademicHours }}</td>
            <td class="text-nowrap">{{ formatPrice(course.price) }}</td>
            <td>{{ course.lecturerNames ?? '—' }}</td>
            <td>{{ course.roomName ?? '—' }}</td>
            <td><CourseStatusBadge :status="course.status" :is-past="course.isPast" /></td>
            <td>{{ course.participantCount }}</td>
            <td><CheckMark :value="course.hasNotes" /></td>
            <td><CheckMark :value="course.hasMeetingLink" /></td>
            <td>
              <div class="d-flex gap-1">
                <RouterLink
                  :to="{
                    name: 'courseFormRoute',
                    query: { returnTo: $route.fullPath, courseId: course.courseId },
                  }"
                  :title="$t('adminTrainingCourses.edit')"
                  :aria-label="$t('adminTrainingCourses.edit')"
                  class="btn btn-sm btn-outline-secondary d-inline-flex"
                >
                  <PhPencilSimple :size="20" />
                </RouterLink>
                <CourseDeleteButton
                  :course-id="course.courseId"
                  :start-date="course.startDate"
                  :end-date="course.endDate"
                  :participant-count="course.participantCount"
                  @event-course-deleted="handleCourseDeleted"
                />
              </div>
            </td>
          </tr>
          <tr v-if="courses.length === 0">
            <td colspan="12" class="text-center text-secondary py-4">{{ emptyTableText }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="text-secondary mb-4">
      {{ $t('adminTrainingCourses.totalCount', courses.length) }}
    </p>
  </div>
</template>

<style scoped>
/* Toimunud toimumiskord: tuhmim rida */
.past-row td {
  color: var(--bs-secondary-color);
}
</style>
