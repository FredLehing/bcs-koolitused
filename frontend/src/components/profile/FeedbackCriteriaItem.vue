<script>
import HelpTip from '@/components/common/HelpTip.vue'

const SCORES = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
const FEEDBACK_TEXT_MAX_LENGTH = 10000

// Tagasiside vormi üks kriteerium: nimi + (?) kirjeldusega, hinne 1–10 ja valikuline kommentaar.
// Kommentaari kast on vaikimisi peidus; olemasoleva kommentaariga kohe lahti. Peitmine teksti ei kustuta.
export default {
  name: 'FeedbackCriteriaItem',
  components: { HelpTip },
  props: {
    criteria: Object,
    answer: Object,
    isReadOnly: Boolean,
    isInvalid: Boolean,
  },
  emits: ['event-score-changed', 'event-feedback-text-changed'],
  data() {
    return {
      isCommentOpen: this.answer.feedbackText !== '',
    }
  },
  computed: {
    scores() {
      return SCORES
    },

    feedbackTextMaxLength() {
      return FEEDBACK_TEXT_MAX_LENGTH
    },

    // Lugemisrežiimis kommentaarita kriteeriumil linki ega kasti pole
    isCommentToggleVisible() {
      return !this.isReadOnly
    },

    isCommentVisible() {
      return this.isCommentOpen && (!this.isReadOnly || this.answer.feedbackText !== '')
    },
  },
  watch: {
    // Uuesti laaditud või taastatud vastus: olemasolev kommentaar on lahti
    'answer.feedbackText'(newFeedbackText) {
      if (newFeedbackText !== '' && this.isReadOnly) {
        this.isCommentOpen = true
      }
    },
  },
  methods: {
    toggleComment() {
      this.isCommentOpen = !this.isCommentOpen
    },
  },
}
</script>

<template>
  <div class="rounded-xl border p-4" :class="isInvalid ? 'border-red-500' : 'border-line'">
    <div class="mb-3 flex items-center justify-between gap-2">
      <span
        :id="`feedback-criteria-${criteria.feedbackCriteriaId}`"
        class="font-semibold text-navy"
      >
        {{ criteria.title }}
      </span>
      <HelpTip :text="criteria.description" class="shrink-0" />
    </div>

    <div
      class="grid grid-cols-5 gap-2 sm:grid-cols-10"
      role="radiogroup"
      :aria-labelledby="`feedback-criteria-${criteria.feedbackCriteriaId}`"
    >
      <div v-for="score in scores" :key="score" class="relative">
        <input
          :id="`feedback-score-${criteria.feedbackCriteriaId}-${score}`"
          :name="`feedback-score-${criteria.feedbackCriteriaId}`"
          :value="score"
          :checked="answer.score === score"
          :disabled="isReadOnly"
          @change="$emit('event-score-changed', score)"
          class="peer sr-only"
          type="radio"
        />
        <label
          :for="`feedback-score-${criteria.feedbackCriteriaId}-${score}`"
          :class="isInvalid ? 'border-red-500' : 'border-brand-200'"
          class="flex min-h-11 cursor-pointer items-center justify-center rounded-lg border bg-white font-semibold text-ink tabular-nums select-none hover:bg-brand-50 peer-checked:border-brand-600 peer-checked:bg-brand-600 peer-checked:text-white peer-focus-visible:ring-3 peer-focus-visible:ring-brand-600/30 peer-disabled:cursor-default peer-disabled:opacity-70"
        >
          {{ score }}
        </label>
      </div>
    </div>

    <button
      v-if="isCommentToggleVisible"
      @click="toggleComment"
      :aria-expanded="isCommentOpen"
      class="btn btn-link btn-sm mt-2 px-0"
      type="button"
    >
      {{
        isCommentOpen ? $t('participantFeedback.hideComment') : $t('participantFeedback.addComment')
      }}
    </button>

    <div v-if="isCommentVisible" class="mt-2">
      <label class="sr-only" :for="`feedback-text-${criteria.feedbackCriteriaId}`">
        {{ $t('participantFeedback.commentLabel') }}: {{ criteria.title }}
      </label>
      <textarea
        :id="`feedback-text-${criteria.feedbackCriteriaId}`"
        :value="answer.feedbackText"
        :disabled="isReadOnly"
        :maxlength="feedbackTextMaxLength"
        :placeholder="$t('participantFeedback.commentPlaceholder')"
        @input="$emit('event-feedback-text-changed', $event.target.value)"
        class="form-control"
        rows="3"
      ></textarea>
      <div class="form-text text-right">
        {{ answer.feedbackText.length }} / {{ feedbackTextMaxLength }}
      </div>
    </div>
  </div>
</template>
