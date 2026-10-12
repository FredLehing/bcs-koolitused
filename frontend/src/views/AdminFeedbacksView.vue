<script>
import { mapState } from 'pinia'
import { PhFunnel, PhMagnifyingGlass } from '@phosphor-icons/vue'
import { useLanguageStore } from '@/stores/languageStore.js'
import AdminFeedbackService from '@/api-services/AdminFeedbackService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import NavigationService from '@/services/NavigationService.js'
import FormatService from '@/services/FormatService.js'
import AdminTabs from '@/components/common/AdminTabs.vue'
import PaginationNav from '@/components/common/PaginationNav.vue'
import SortableColumnHeader from '@/components/common/SortableColumnHeader.vue'
import InlineAlerts from '@/components/common/InlineAlerts.vue'

const emptyFilters = () => ({
  courseId: '',
  status: '',
  comments: '',
  from: '',
  until: '',
  low: '',
})

export default {
  name: 'AdminFeedbacksView',
  components: {
    AdminTabs,
    PaginationNav,
    SortableColumnHeader,
    InlineAlerts,
    PhFunnel,
    PhMagnifyingGlass,
  },
  data() {
    return {
      searchText: '',
      appliedSearch: '',
      draftFilters: emptyFilters(),
      appliedFilters: emptyFilters(),
      showFilters: false,
      validationMessage: '',
      sortBy: 'default',
      sortDirection: 'desc',
      page: 0,
      limit: 15,
      pageData: null,
      courses: [],
      loading: false,
      coursesLoading: false,
      detailLoading: false,
      reviewing: false,
      openFeedbackId: null,
      detail: null,
      errorMessage: '',
      courseError: '',
      detailError: '',
      successMessage: '',
      pageRequest: 0,
      courseRequest: 0,
      detailRequest: 0,
      active: false,
      destroyed: false,
    }
  },
  computed: {
    ...mapState(useLanguageStore, ['contentLang']),
    hasFilters() {
      return (
        this.appliedSearch !== '' ||
        Object.values(this.appliedFilters).some((value) => value !== '')
      )
    },
    sortableColumns() {
      return [
        'createdAt',
        'answersUpdatedAt',
        'trainingTitle',
        'participantName',
        'averageScore',
        'minimumScore',
      ]
    },
    summaryCards() {
      if (!this.pageData) return []
      const data = this.pageData
      const rate = data.courseResponseRate
      return [
        { label: this.$t('adminFeedbacks.total'), value: data.totalElements, tone: 'text-navy' },
        {
          label: this.$t('adminFeedbacks.needsReview'),
          value: data.needsReviewCount,
          tone: 'text-amber-700',
        },
        {
          label: this.$t('adminFeedbacks.lowScores'),
          value: data.lowScoreFeedbackCount,
          tone: 'text-red-700',
        },
        {
          tone: 'text-brand-700',
          label: this.$t(rate ? 'adminFeedbacks.responseRate' : 'adminFeedbacks.average'),
          value: rate
            ? `${rate.respondedCount} / ${rate.registeredCount}`
            : this.number(data.overallAverageScore),
          extra:
            rate && rate.responsePercentage !== null
              ? `${this.number(rate.responsePercentage)}%`
              : '',
        },
      ]
    },
  },
  watch: {
    contentLang() {
      if (!this.active) return
      const openFeedbackId = this.openFeedbackId
      this.loadCourses()
      this.loadPage(true)
      if (openFeedbackId !== null) this.loadDetail(openFeedbackId)
    },
  },
  methods: {
    statusBadgeClass(status) {
      return { H: 'text-bg-success', U: 'text-bg-warning' }[status] ?? 'text-bg-primary'
    },

    // Kriteeriumi keskmise riba laius (hinded 1–10)
    scoreBarWidth(averageScore) {
      return `${Math.max(0, Math.min(10, averageScore ?? 0)) * 10}%`
    },

    parameters() {
      const parameters = {
        contentLang: this.contentLang,
        searchText: this.appliedSearch,
        ...this.appliedFilters,
        sortBy: this.sortBy,
        sortDirection: this.sortDirection,
        page: this.page,
        limit: this.limit,
      }
      return Object.fromEntries(Object.entries(parameters).filter(([, value]) => value !== ''))
    },
    loadCourses() {
      const request = ++this.courseRequest
      this.coursesLoading = true
      this.courseError = ''
      return AdminFeedbackService.sendGetAdminFeedbackCoursesRequest(this.contentLang)
        .then((response) => {
          if (this.destroyed || request !== this.courseRequest) return
          this.courses = response.data
        })
        .catch((error) => {
          if (this.destroyed || request !== this.courseRequest) return
          this.courses = []
          this.courseError = this.errorText(error)
        })
        .finally(() => {
          if (!this.destroyed && request === this.courseRequest) this.coursesLoading = false
        })
    },
    loadPage(preserveDetail = false) {
      if (!preserveDetail) this.closeDetail()
      const request = ++this.pageRequest
      this.loading = true
      this.errorMessage = ''
      this.pageData = null
      return AdminFeedbackService.sendGetAdminFeedbacksRequest(this.parameters())
        .then((response) => {
          if (this.destroyed || request !== this.pageRequest) return
          const data = response.data
          if (this.page > 0 && this.page >= data.totalPages) {
            this.page = Math.max(0, data.totalPages - 1)
            return this.loadPage(preserveDetail)
          }
          this.pageData = data
          if (preserveDetail && !data.content.some((row) => row.feedbackId === this.openFeedbackId))
            this.closeDetail()
        })
        .catch((error) => {
          if (this.destroyed || request !== this.pageRequest) return
          this.handleError(error)
        })
        .finally(() => {
          if (!this.destroyed && request === this.pageRequest) this.loading = false
        })
    },
    applySearch() {
      if (this.reviewing) return
      this.appliedSearch = this.searchText.trim()
      this.page = 0
      this.loadPage()
    },
    applyFilters() {
      if (this.reviewing) return
      if (
        this.draftFilters.from &&
        this.draftFilters.until &&
        this.draftFilters.from > this.draftFilters.until
      ) {
        this.validationMessage = this.$t('adminFeedbacks.invalidDates')
        return
      }
      this.validationMessage = ''
      this.appliedFilters = { ...this.draftFilters }
      this.page = 0
      this.loadPage()
    },
    clearFilters() {
      if (this.reviewing) return
      this.searchText = this.appliedSearch = ''
      this.draftFilters = emptyFilters()
      this.appliedFilters = emptyFilters()
      this.validationMessage = ''
      this.page = 0
      this.loadPage()
    },
    sort(sortKey) {
      if (this.reviewing) return
      this.sortDirection = this.sortBy === sortKey && this.sortDirection === 'asc' ? 'desc' : 'asc'
      this.sortBy = sortKey
      this.page = 0
      this.loadPage()
    },
    changePage(page) {
      if (this.reviewing) return
      this.page = page
      this.loadPage()
    },
    closeDetail() {
      ++this.detailRequest
      this.openFeedbackId = null
      this.detail = null
      this.detailError = ''
      this.detailLoading = false
    },
    toggleDetail(feedbackId) {
      if (this.reviewing) return
      if (this.openFeedbackId === feedbackId) this.closeDetail()
      else this.loadDetail(feedbackId)
    },
    loadDetail(feedbackId) {
      const request = ++this.detailRequest
      this.openFeedbackId = feedbackId
      this.detail = null
      this.detailError = ''
      this.detailLoading = true
      return AdminFeedbackService.sendGetAdminFeedbackRequest(feedbackId, this.contentLang)
        .then((response) => {
          if (!this.destroyed && request === this.detailRequest) this.detail = response.data
        })
        .catch((error) => {
          if (this.destroyed || request !== this.detailRequest) return
          this.detailError = this.errorText(error)
          if (error.response?.status === 404) this.$router.push({ name: 'errorRoute' })
        })
        .finally(() => {
          if (!this.destroyed && request === this.detailRequest) this.detailLoading = false
        })
    },
    canReview(row) {
      return (
        !this.reviewing &&
        !this.detailLoading &&
        this.detail?.feedbackId === row.feedbackId &&
        ['N', 'U'].includes(this.detail.status)
      )
    },
    review(row) {
      if (!this.canReview(row)) return
      const feedbackId = row.feedbackId
      const answersVersion = this.detail.answersVersion
      this.reviewing = true
      this.errorMessage = ''
      this.successMessage = ''
      return AdminFeedbackService.sendPutAdminFeedbackReviewRequest(feedbackId, answersVersion)
        .then(() => {
          if (this.destroyed) return
          this.successMessage = this.$t('adminFeedbacks.reviewSuccess')
          return this.loadPage()
        })
        .catch((error) => {
          if (this.destroyed) return
          this.handleError(error)
          if (error.response?.status === 409) {
            return Promise.all([this.loadDetail(feedbackId), this.loadPage(true)]).then(() => {
              this.errorMessage = this.errorText(error)
            })
          }
        })
        .finally(() => {
          if (!this.destroyed) this.reviewing = false
        })
    },
    handleError(error) {
      this.errorMessage = this.errorText(error)
      if (error.response?.status === 404) this.$router.push({ name: 'errorRoute' })
    },
    errorText(error) {
      return error.response?.status < 500 && error.response?.data?.message
        ? error.response.data.message
        : this.$t('adminFeedbacks.loadError')
    },
    number(value) {
      return value == null
        ? '—'
        : new Intl.NumberFormat(this.contentLang, {
            minimumFractionDigits: 1,
            maximumFractionDigits: 1,
          }).format(value)
    },
    dateTime(value) {
      return value ? FormatService.formatDateTime(value) : '—'
    },
    dateRange(course) {
      return FormatService.formatDateRange(course.startDate, course.endDate)
    },
    statusLabel(status) {
      return this.$t(`adminFeedbacks.statuses.${status}`)
    },
    rowStatus(row) {
      return this.detail?.feedbackId === row.feedbackId ? this.detail.status : row.status
    },
  },
  beforeMount() {
    if (!SessionStorageService.userIsAdmin()) {
      NavigationService.navigateToNotAuthorizedView()
      return
    }
    this.active = true
    this.loadCourses()
    this.loadPage()
  },
  beforeUnmount() {
    this.destroyed = true
    ++this.courseRequest
    ++this.pageRequest
    ++this.detailRequest
  },
}
</script>

<template>
  <div class="mx-auto w-full max-w-7xl px-6 py-8">
    <AdminTabs />
    <div class="mb-6 flex items-end justify-between gap-4">
      <h1 class="text-3xl font-extrabold tracking-tight">{{ $t('adminFeedbacks.title') }}</h1>
      <InlineAlerts
        :success-message="successMessage"
        :error-message="errorMessage"
        @event-success-message-closed="successMessage = ''"
        @event-error-message-closed="errorMessage = ''"
      />
    </div>

    <div v-if="pageData" class="mb-6 grid grid-cols-4 gap-4">
      <div
        v-for="card in summaryCards"
        :key="card.label"
        class="rounded-2xl border border-line bg-white px-5 py-4"
      >
        <div class="text-sm font-semibold text-muted">{{ card.label }}</div>
        <div class="mt-1 flex items-baseline gap-2">
          <span :class="card.tone" class="font-display text-3xl font-extrabold tabular-nums">
            {{ card.value }}
          </span>
          <span v-if="card.extra" class="text-sm text-muted">{{ card.extra }}</span>
        </div>
      </div>
    </div>

    <form class="mb-4 flex items-center gap-2" @submit.prevent="applySearch">
      <div
        class="flex min-h-11 max-w-xl flex-1 items-center gap-2 rounded-lg border border-brand-200 bg-white px-3 focus-within:border-brand-600 focus-within:ring-3 focus-within:ring-brand-600/15"
      >
        <PhMagnifyingGlass :size="18" class="shrink-0 text-muted" />
        <input
          v-model="searchText"
          :placeholder="$t('adminFeedbacks.searchPlaceholder')"
          :aria-label="$t('adminFeedbacks.searchPlaceholder')"
          :disabled="reviewing"
          type="search"
          class="min-w-0 flex-1 bg-transparent outline-none"
        />
      </div>
      <button class="btn btn-primary" :disabled="reviewing" type="submit">
        {{ $t('adminFeedbacks.search') }}
      </button>
      <button
        :class="{ 'border-brand-600 bg-brand-50': showFilters }"
        class="btn btn-outline-secondary"
        type="button"
        :aria-expanded="showFilters"
        aria-controls="feedback-filters"
        @click="showFilters = !showFilters"
      >
        <PhFunnel :size="18" />
        {{ $t(showFilters ? 'adminFeedbacks.hideFilters' : 'adminFeedbacks.showFilters') }}
      </button>
      <button
        v-if="hasFilters"
        class="btn btn-link btn-sm"
        type="button"
        :disabled="reviewing"
        @click="clearFilters"
      >
        {{ $t('adminFeedbacks.clearFilters') }}
      </button>
    </form>

    <form
      v-show="showFilters"
      id="feedback-filters"
      class="card mb-4"
      @submit.prevent="applyFilters"
    >
      <fieldset :disabled="reviewing" class="card-body">
        <legend class="visually-hidden">{{ $t('adminFeedbacks.showFilters') }}</legend>
        <div class="grid grid-cols-6 gap-4">
          <div class="col-span-3">
            <label for="feedback-course" class="form-label">{{
              $t('adminFeedbacks.course')
            }}</label>
            <select
              id="feedback-course"
              v-model="draftFilters.courseId"
              class="form-select"
              :disabled="coursesLoading || !!courseError"
            >
              <option value="">{{ $t('adminFeedbacks.all') }}</option>
              <option v-for="course in courses" :key="course.courseId" :value="course.courseId">
                {{ course.trainingTitle }} — {{ dateRange(course) }}
              </option>
            </select>
            <small v-if="coursesLoading" class="form-text block" role="status">
              {{ $t('adminFeedbacks.loading') }}
            </small>
            <div v-if="courseError" class="mt-1 text-sm text-red-700" role="alert">
              {{ courseError }}
              <button class="btn btn-link btn-sm" type="button" @click="loadCourses">
                {{ $t('adminFeedbacks.retry') }}
              </button>
            </div>
          </div>
          <div class="col-span-3 grid grid-cols-2 gap-4">
            <div>
              <label for="feedback-status" class="form-label">
                {{ $t('adminFeedbacks.columns.status') }}
              </label>
              <select id="feedback-status" v-model="draftFilters.status" class="form-select">
                <option value="">{{ $t('adminFeedbacks.all') }}</option>
                <option value="pending">{{ $t('adminFeedbacks.needsReview') }}</option>
                <option v-for="status in ['N', 'U', 'H']" :key="status" :value="status">
                  {{ statusLabel(status) }}
                </option>
              </select>
            </div>
            <div>
              <label for="feedback-comments" class="form-label">
                {{ $t('adminFeedbacks.columns.commentCount') }}
              </label>
              <select id="feedback-comments" v-model="draftFilters.comments" class="form-select">
                <option value="">{{ $t('adminFeedbacks.all') }}</option>
                <option value="yes">{{ $t('adminFeedbacks.withComments') }}</option>
                <option value="no">{{ $t('adminFeedbacks.noComments') }}</option>
              </select>
            </div>
          </div>
          <div class="col-span-2">
            <label for="feedback-from" class="form-label">{{ $t('adminFeedbacks.from') }}</label>
            <input
              id="feedback-from"
              v-model="draftFilters.from"
              type="date"
              class="form-control"
              :aria-invalid="!!validationMessage"
              aria-describedby="feedback-date-error"
            />
          </div>
          <div class="col-span-2">
            <label for="feedback-until" class="form-label">{{ $t('adminFeedbacks.until') }}</label>
            <input
              id="feedback-until"
              v-model="draftFilters.until"
              type="date"
              class="form-control"
              :aria-invalid="!!validationMessage"
              aria-describedby="feedback-date-error"
            />
          </div>
          <div class="col-span-2">
            <label for="feedback-low" class="form-label">{{ $t('adminFeedbacks.low') }}</label>
            <select id="feedback-low" v-model="draftFilters.low" class="form-select">
              <option value="">{{ $t('adminFeedbacks.all') }}</option>
              <option v-for="low in [3, 5, 7]" :key="low" :value="low">{{ low }}</option>
            </select>
          </div>
        </div>
        <div id="feedback-date-error" class="mt-2 text-sm text-red-700" role="alert">
          {{ validationMessage }}
        </div>
        <button class="btn btn-primary mt-3" type="submit">
          {{ $t('adminFeedbacks.filter') }}
        </button>
      </fieldset>
    </form>

    <p v-if="loading" class="py-6 text-muted" role="status">{{ $t('adminFeedbacks.loading') }}</p>
    <button
      v-if="!loading && !pageData"
      class="btn btn-outline-secondary mb-4"
      type="button"
      @click="loadPage()"
    >
      {{ $t('adminFeedbacks.retry') }}
    </button>

    <template v-if="pageData && !loading">
      <section
        class="mb-6 rounded-2xl border border-line bg-white p-5"
        aria-labelledby="criteria-heading"
      >
        <h2 id="criteria-heading" class="mb-4 text-lg font-bold">
          {{ $t('adminFeedbacks.criteriaAverages') }}
        </h2>
        <div class="grid grid-cols-3 gap-x-8 gap-y-4">
          <div v-for="criterion in pageData.criteriaAverages" :key="criterion.feedbackCriteriaId">
            <div class="mb-1.5 flex items-baseline justify-between gap-2">
              <span class="truncate text-[15px] font-semibold" :title="criterion.title">
                {{ criterion.title }}
              </span>
              <span class="shrink-0 font-display font-extrabold text-navy tabular-nums">
                {{ number(criterion.averageScore) }}
              </span>
            </div>
            <div class="h-2 overflow-hidden rounded-full bg-brand-100">
              <div
                :style="{ width: scoreBarWidth(criterion.averageScore) }"
                :class="
                  criterion.averageScore != null && criterion.averageScore <= 5
                    ? 'bg-red-500'
                    : 'bg-brand-600'
                "
                class="h-full rounded-full"
              ></div>
            </div>
            <div class="mt-1 text-xs text-muted">
              {{ $t('adminFeedbacks.answerCount', { count: criterion.answerCount }) }}
            </div>
          </div>
        </div>
      </section>

      <div class="overflow-hidden rounded-2xl border border-line bg-white" :aria-busy="reviewing">
        <div class="overflow-x-auto">
          <table class="table table-hover">
            <caption class="visually-hidden">
              {{
                $t('adminFeedbacks.title')
              }}
            </caption>
            <thead class="bg-surface">
              <tr>
                <SortableColumnHeader
                  v-for="column in sortableColumns"
                  :key="column"
                  :label="$t(`adminFeedbacks.columns.${column}`)"
                  :sort-key="column"
                  :sort-by="sortBy"
                  :sort-direction="sortDirection"
                  @event-sort-clicked="sort"
                />
                <th>{{ $t('adminFeedbacks.columns.commentCount') }}</th>
                <SortableColumnHeader
                  :label="$t('adminFeedbacks.columns.status')"
                  sort-key="status"
                  :sort-by="sortBy"
                  :sort-direction="sortDirection"
                  @event-sort-clicked="sort"
                />
                <th class="text-right">{{ $t('adminFeedbacks.columns.actions') }}</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="row in pageData.content" :key="row.feedbackId">
                <tr :class="{ 'bg-brand-50/60': openFeedbackId === row.feedbackId }">
                  <td class="whitespace-nowrap tabular-nums">{{ dateTime(row.createdAt) }}</td>
                  <td class="whitespace-nowrap text-muted tabular-nums">
                    {{ dateTime(row.answersUpdatedAt) }}
                  </td>
                  <td>
                    <RouterLink
                      :to="{
                        name: 'adminCourseRoute',
                        query: { courseId: row.courseId, returnTo: '/admin-feedbacks' },
                      }"
                      class="font-semibold"
                      >{{ row.trainingTitle }}</RouterLink
                    ><small class="block text-sm whitespace-nowrap text-muted">{{
                      dateRange(row)
                    }}</small>
                  </td>
                  <td>
                    <RouterLink
                      :to="{
                        name: 'adminRegistrationRoute',
                        query: {
                          courseParticipantId: row.courseParticipantId,
                          returnTo: '/admin-feedbacks',
                        },
                      }"
                      >{{ row.participantName }}</RouterLink
                    >
                  </td>
                  <td class="font-semibold tabular-nums">{{ number(row.averageScore) }}</td>
                  <td class="tabular-nums">
                    <span
                      v-if="row.minimumScore != null && row.minimumScore <= 5"
                      class="inline-flex min-w-7 justify-center rounded-md bg-red-100 px-1.5 font-bold text-red-800"
                      >{{ row.minimumScore
                      }}<span class="visually-hidden">
                        — {{ $t('adminFeedbacks.lowScores') }}</span
                      ></span
                    >
                    <template v-else>{{ row.minimumScore ?? '—' }}</template>
                  </td>
                  <td class="tabular-nums">{{ row.commentCount }}</td>
                  <td>
                    <span
                      class="badge"
                      :class="statusBadgeClass(rowStatus(row))"
                      :title="
                        rowStatus(row) === 'U'
                          ? $t('adminFeedbacks.updatedHint')
                          : statusLabel(rowStatus(row))
                      "
                      >{{ statusLabel(rowStatus(row)) }}</span
                    >
                  </td>
                  <td>
                    <div class="flex justify-end gap-2">
                      <button
                        type="button"
                        class="btn btn-outline-secondary btn-sm"
                        :disabled="reviewing"
                        :aria-expanded="openFeedbackId === row.feedbackId"
                        :aria-controls="`feedback-detail-${row.feedbackId}`"
                        @click="toggleDetail(row.feedbackId)"
                      >
                        {{
                          $t(
                            openFeedbackId === row.feedbackId
                              ? 'adminFeedbacks.hideAnswers'
                              : 'adminFeedbacks.openAnswers',
                          )
                        }}
                      </button>
                      <button
                        v-if="rowStatus(row) !== 'H'"
                        type="button"
                        class="btn btn-outline-success btn-sm"
                        :disabled="!canReview(row)"
                        :title="$t('adminFeedbacks.reviewHint')"
                        @click="review(row)"
                      >
                        {{ $t('adminFeedbacks.review') }}
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="openFeedbackId === row.feedbackId">
                  <td
                    :id="`feedback-detail-${row.feedbackId}`"
                    colspan="9"
                    class="bg-brand-50/60 px-6 py-5"
                  >
                    <p v-if="detailLoading" class="text-muted" role="status">
                      {{ $t('adminFeedbacks.loading') }}
                    </p>
                    <div v-else-if="detailError" class="text-red-700" role="alert">
                      {{ detailError }}
                      <button
                        class="btn btn-link btn-sm"
                        type="button"
                        @click="loadDetail(row.feedbackId)"
                      >
                        {{ $t('adminFeedbacks.retry') }}
                      </button>
                    </div>
                    <template v-else-if="detail">
                      <h2 class="mb-3 text-base font-bold">
                        {{ row.participantName }} — {{ $t('adminFeedbacks.answers') }}
                      </h2>
                      <div class="overflow-hidden rounded-xl border border-line bg-white">
                        <table class="table feedback-answers">
                          <thead class="bg-surface">
                            <tr>
                              <th class="feedback-criterion">
                                {{ $t('adminFeedbacks.criterion') }}
                              </th>
                              <th class="feedback-score">{{ $t('adminFeedbacks.score') }}</th>
                              <th>{{ $t('adminFeedbacks.comment') }}</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="answer in detail.criteria" :key="answer.feedbackCriteriaId">
                              <td class="feedback-criterion font-semibold">{{ answer.title }}</td>
                              <td
                                class="feedback-score whitespace-nowrap tabular-nums"
                                :class="{ 'font-bold text-red-700': answer.score <= 5 }"
                              >
                                {{ answer.score }} / 10
                              </td>
                              <td class="feedback-comment">
                                {{ answer.feedbackText?.trim() ? answer.feedbackText : '—' }}
                              </td>
                            </tr>
                            <tr v-if="detail.criteria.length === 0">
                              <td colspan="3">{{ $t('adminFeedbacks.noAnswers') }}</td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </template>
                  </td>
                </tr>
              </template>
              <tr v-if="pageData.content.length === 0">
                <td colspan="9" class="py-10 text-center text-muted">
                  {{ $t('adminFeedbacks.noResults') }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <PaginationNav
        :page="page"
        :total-pages="pageData.totalPages"
        @event-page-changed="changePage"
        class="mt-6"
      />
    </template>
  </div>
</template>

<style scoped>
.feedback-answers {
  table-layout: fixed;
}
.feedback-criterion {
  width: 14rem;
  overflow-wrap: anywhere;
}
.feedback-score {
  width: 6rem;
}
.feedback-comment {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
</style>
