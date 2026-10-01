<script>
import { Tooltip } from 'bootstrap'
import { PhQuestion } from '@phosphor-icons/vue'

const SCORES = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
const FEEDBACK_TEXT_MAX_LENGTH = 255

// Tagasiside vormi üks kriteerium: nimi + (?) kirjeldusega, hinne 1–10 ja valikuline kommentaar.
// Kommentaari kast on vaikimisi peidus; olemasoleva kommentaariga kohe lahti. Peitmine teksti ei kustuta.
export default {
  name: 'FeedbackCriteriaItem',
  components: { PhQuestion },
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
      descriptionTooltip: null,
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

    'criteria.description'() {
      this.$nextTick(() => this.updateDescriptionTooltip())
    },
  },
  methods: {
    toggleComment() {
      this.isCommentOpen = !this.isCommentOpen
    },

    // Bootstrap tooltip loeb teksti ainult loomisel, keele vahetusel tuleb see uuendada
    updateDescriptionTooltip() {
      this.descriptionTooltip.setContent({ '.tooltip-inner': this.criteria.description })
    },
  },
  mounted() {
    this.descriptionTooltip = new Tooltip(this.$refs.descriptionHelp)
  },
  beforeUnmount() {
    this.descriptionTooltip.dispose()
  },
}
</script>

<template>
  <div class="border rounded p-3" :class="{ 'border-danger': isInvalid }">
    <div class="d-flex align-items-center gap-2 mb-2">
      <span :id="`feedback-criteria-${criteria.feedbackCriteriaId}`" class="fw-semibold">
        {{ criteria.title }}
      </span>
      <span
        ref="descriptionHelp"
        class="text-secondary"
        role="img"
        tabindex="0"
        data-bs-toggle="tooltip"
        :data-bs-title="criteria.description"
        :aria-label="criteria.description"
      >
        <PhQuestion :size="18" />
      </span>
    </div>

    <div
      class="d-flex flex-wrap gap-3"
      role="radiogroup"
      :aria-labelledby="`feedback-criteria-${criteria.feedbackCriteriaId}`"
    >
      <div v-for="score in scores" :key="score" class="form-check form-check-inline m-0">
        <input
          :id="`feedback-score-${criteria.feedbackCriteriaId}-${score}`"
          :name="`feedback-score-${criteria.feedbackCriteriaId}`"
          :value="score"
          :checked="answer.score === score"
          :disabled="isReadOnly"
          @change="$emit('event-score-changed', score)"
          class="form-check-input"
          :class="{ 'is-invalid': isInvalid }"
          type="radio"
        />
        <label
          class="form-check-label"
          :for="`feedback-score-${criteria.feedbackCriteriaId}-${score}`"
        >
          {{ score }}
        </label>
      </div>
    </div>

    <button
      v-if="isCommentToggleVisible"
      @click="toggleComment"
      :aria-expanded="isCommentOpen"
      class="btn btn-link btn-sm px-0 mt-2"
      type="button"
    >
      {{
        isCommentOpen ? $t('participantFeedback.hideComment') : $t('participantFeedback.addComment')
      }}
    </button>

    <div v-if="isCommentVisible" class="mt-2">
      <label class="visually-hidden" :for="`feedback-text-${criteria.feedbackCriteriaId}`">
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
      <div class="form-text text-end">
        {{ answer.feedbackText.length }} / {{ feedbackTextMaxLength }}
      </div>
    </div>
  </div>
</template>
