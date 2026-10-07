<script>
import { PhChatCircleDots, PhX } from '@phosphor-icons/vue'
import ChatbotService from '@/api-services/ChatbotService.js'
import { useLanguageStore } from '@/stores/languageStore.js'

const MAX_SESSION_HISTORY_MESSAGES = 18
const MAX_HISTORY_MESSAGE_LENGTH = 4000

export default {
  name: 'ChatbotWidget',

  components: {
    PhChatCircleDots,
    PhX,
  },

  data() {
    return {
      isOpen: false,
      isLoading: false,
      question: '',
      errorMessage: '',
      conversationHistory: [],
      messages: [
        {
          role: 'assistant',
          text: this.$t('chatbot.greeting'),
        },
      ],
    }
  },

  methods: {
    toggleChat() {
      this.isOpen = !this.isOpen

      if (this.isOpen) {
        this.scrollToBottom()
        this.focusInput()
      }
    },

    async sendQuestion() {
      const question = this.question.trim()

      if (!question || this.isLoading) {
        return
      }

      this.messages.push({
        role: 'user',
        text: question,
      })

      this.question = ''
      this.errorMessage = ''
      this.isLoading = true

      this.scrollToBottom()

      try {
        const languageStore = useLanguageStore()

        const previousMessages = this.conversationHistory.slice(-MAX_SESSION_HISTORY_MESSAGES)

        const response = await ChatbotService.sendQuestionRequest(
          question,
          languageStore.contentLang,
          previousMessages,
        )

        const answer = response.data.answer

        if (response.data.sessionEnded) {
          this.startNewSession(answer)
          return
        }

        this.messages.push({
          role: 'assistant',
          text: answer,
        })

        this.addToHistory('user', question)
        this.addToHistory('assistant', answer)
      } catch (error) {
        this.errorMessage = error.response?.data?.message || this.$t('chatbot.error')
      } finally {
        this.isLoading = false
        this.scrollToBottom()
        this.focusInput()
      }
    },

    startNewSession(handoffMessage) {
      this.conversationHistory = []
      this.question = ''

      this.messages = [
        {
          role: 'assistant',
          text: handoffMessage,
        },
        {
          role: 'assistant',
          text: this.$t('chatbot.greeting'),
        },
      ]
    },

    addToHistory(role, text) {
      this.conversationHistory.push({
        role,
        text: text.slice(0, MAX_HISTORY_MESSAGE_LENGTH),
      })
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const messagesElement = this.$refs.messages

        if (messagesElement) {
          messagesElement.scrollTop = messagesElement.scrollHeight
        }
      })
    },

    focusInput() {
      this.$nextTick(() => {
        this.$refs.questionInput?.focus()
      })
    },
  },
}
</script>

<template>
  <div class="chatbot-widget">
    <div
      v-if="isOpen"
      v-motion
      :initial="{
        opacity: 0,
        y: 30,
        scale: 0.96,
      }"
      :enter="{
        opacity: 1,
        y: 0,
        scale: 1,
        transition: {
          type: 'spring',
          stiffness: 260,
          damping: 22,
        },
      }"
      class="chatbot-panel shadow-lg"
    >
      <header class="chatbot-header">
        <div class="chatbot-title">
          <div class="chatbot-avatar">
            <PhChatCircleDots :size="24" weight="fill" />
          </div>

          <div>
            <div class="chatbot-name">Chatbot</div>

            <div class="chatbot-status">BCS Koolitused</div>
          </div>
        </div>

        <button
          type="button"
          class="chatbot-close"
          :aria-label="$t('chatbot.close')"
          @click="toggleChat"
        >
          <PhX :size="20" />
        </button>
      </header>

      <div ref="messages" class="chatbot-messages">
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="chatbot-message"
          :class="message.role === 'user' ? 'chatbot-message-user' : 'chatbot-message-assistant'"
        >
          {{ message.text }}
        </div>

        <div v-if="isLoading" class="chatbot-loading">
          {{ $t('chatbot.loading') }}
        </div>
      </div>

      <div v-if="errorMessage" class="chatbot-error">
        {{ errorMessage }}
      </div>

      <form class="chatbot-input" @submit.prevent="sendQuestion">
        <input
          ref="questionInput"
          v-model="question"
          type="text"
          maxlength="500"
          :placeholder="$t('chatbot.placeholder')"
          :disabled="isLoading"
          autocomplete="off"
        />
      </form>
    </div>

    <button
      v-motion
      :initial="{
        scale: 0,
        opacity: 0,
      }"
      :enter="{
        scale: 1,
        opacity: 1,
        transition: {
          type: 'spring',
          stiffness: 300,
          damping: 18,
        },
      }"
      :hovered="{ scale: 1.08 }"
      :tapped="{ scale: 0.92 }"
      type="button"
      class="chatbot-button btn btn-primary shadow"
      :aria-label="isOpen ? $t('chatbot.close') : $t('chatbot.open')"
      @click="toggleChat"
    >
      <PhX v-if="isOpen" :size="28" />

      <PhChatCircleDots v-else :size="30" weight="fill" />
    </button>
  </div>
</template>

<style scoped>
.chatbot-widget {
  position: fixed;
  right: 24px;
  bottom: 16px;
  z-index: 1040;
}

.chatbot-panel {
  position: absolute;
  right: 0;
  bottom: 80px;

  display: flex;
  flex-direction: column;

  width: min(390px, calc(100vw - 32px));
  height: min(560px, calc(100vh - 120px));

  overflow: hidden;

  border: 1px solid var(--bs-border-color);
  border-radius: 16px;

  background: var(--bs-body-bg);
  color: var(--bs-body-color);
}

.chatbot-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  min-height: 68px;
  padding: 12px 16px;

  border-bottom: 1px solid var(--bs-border-color);

  background: var(--bs-primary);
  color: white;
}

.chatbot-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chatbot-avatar {
  display: flex;
  align-items: center;
  justify-content: center;

  width: 40px;
  height: 40px;

  border-radius: 50%;

  background: rgba(255, 255, 255, 0.16);
}

.chatbot-name {
  font-size: 0.95rem;
  font-weight: 600;
  line-height: 1.2;
}

.chatbot-status {
  margin-top: 2px;

  font-size: 0.75rem;

  opacity: 0.82;
}

.chatbot-close {
  display: flex;
  align-items: center;
  justify-content: center;

  width: 34px;
  height: 34px;

  padding: 0;

  border: 0;
  border-radius: 50%;

  background: transparent;
  color: inherit;
}

.chatbot-close:hover {
  background: rgba(255, 255, 255, 0.12);
}

.chatbot-messages {
  flex: 1;

  min-height: 0;
  padding: 16px;

  overflow-y: auto;
  overflow-x: hidden;

  background: var(--bs-tertiary-bg);

  scrollbar-width: thin;
}

.chatbot-message {
  width: fit-content;
  max-width: 85%;

  margin-bottom: 10px;
  padding: 9px 12px;

  border-radius: 14px;

  font-size: 0.88rem;
  line-height: 1.4;

  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.chatbot-message-user {
  margin-left: auto;

  border-bottom-right-radius: 4px;

  background: var(--bs-primary);
  color: white;
}

.chatbot-message-assistant {
  margin-right: auto;

  border: 1px solid var(--bs-border-color);
  border-bottom-left-radius: 4px;

  background: var(--bs-body-bg);
}

.chatbot-loading {
  padding: 4px 2px 12px;

  font-size: 0.8rem;

  color: var(--bs-secondary-color);
}

.chatbot-error {
  padding: 8px 16px;

  border-top: 1px solid var(--bs-border-color);

  font-size: 0.8rem;

  color: var(--bs-danger);
}

.chatbot-input {
  padding: 12px;

  border-top: 1px solid var(--bs-border-color);

  background: var(--bs-body-bg);
}

.chatbot-input input {
  width: 100%;
  height: 40px;

  padding: 0 12px;

  border: 1px solid var(--bs-border-color);
  border-radius: 10px;

  background: var(--bs-body-bg);
  color: var(--bs-body-color);

  font-size: 0.88rem;

  outline: none;
}

.chatbot-input input:focus {
  border-color: var(--bs-primary);

  box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.12);
}

.chatbot-input input:disabled {
  opacity: 0.65;
}

.chatbot-button {
  display: flex;
  align-items: center;
  justify-content: center;

  width: 60px;
  height: 60px;

  padding: 0;

  border-radius: 50%;
}

@media (max-width: 576px) {
  .chatbot-widget {
    right: 12px;
    bottom: 12px;
  }

  .chatbot-panel {
    right: 0;
    bottom: 72px;

    width: calc(100vw - 24px);
    height: min(520px, calc(100vh - 100px));
  }

  .chatbot-button {
    width: 56px;
    height: 56px;
  }
}
</style>
