<script>
import { PhChatCircleDots, PhPaperPlaneTilt, PhX } from '@phosphor-icons/vue'
import ChatbotService from '@/api-services/ChatbotService.js'

export default {
  name: 'ChatbotWidget',

  components: {
    PhChatCircleDots,
    PhPaperPlaneTilt,
    PhX,
  },

  data() {
    return {
      isOpen: false,
      question: '',
      isLoading: false,
      messages: [
        {
          role: 'assistant',
          text: 'Tere! Küsi minult BCS koolituste kohta.',
        },
      ],
    }
  },

  methods: {
    toggleChat() {
      this.isOpen = !this.isOpen

      if (this.isOpen) {
        this.scrollToBottom()
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
      this.isLoading = true

      this.scrollToBottom()

      try {
        const response = await ChatbotService.sendQuestionRequest(question)

        this.messages.push({
          role: 'assistant',
          text: response.data.answer,
        })
      } catch (error) {
        console.error('Chatbot request failed:', error)

        this.messages.push({
          role: 'assistant',
          text: 'Vabandust, vastuse saamine ebaõnnestus.',
        })
      } finally {
        this.isLoading = false
        this.scrollToBottom()
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const messagesElement = this.$refs.messages

        if (messagesElement) {
          messagesElement.scrollTop = messagesElement.scrollHeight
        }
      })
    },
  },
}
</script>

<template>
  <div class="chatbot-widget">
    <!-- Popout chat window -->
    <div
      v-if="isOpen"
      v-motion
      :initial="{
        opacity: 0,
        y: 30,
        scale: 0.92,
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
      class="chatbot-panel card shadow-lg"
    >
      <!-- Header -->
      <div class="chatbot-header card-header d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-2">
          <PhChatCircleDots :size="26" />
          <strong>BCS assistent</strong>
        </div>

        <button
          type="button"
          class="btn btn-sm btn-light"
          aria-label="Sulge vestlus"
          @click="toggleChat"
        >
          <PhX :size="20" />
        </button>
      </div>

      <!-- Messages -->
      <div ref="messages" class="chatbot-messages card-body">
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="d-flex mb-3"
          :class="message.role === 'user' ? 'justify-content-end' : 'justify-content-start'"
        >
          <div
            class="chatbot-message"
            :class="message.role === 'user' ? 'chatbot-message-user' : 'chatbot-message-assistant'"
          >
            {{ message.text }}
          </div>
        </div>

        <div v-if="isLoading" class="text-body-secondary small">Assistent otsib vastust...</div>
      </div>

      <!-- Input -->
      <div class="card-footer">
        <form class="d-flex gap-2" @submit.prevent="sendQuestion">
          <input
            v-model="question"
            type="text"
            class="form-control"
            maxlength="500"
            placeholder="Küsi koolituste kohta..."
            :disabled="isLoading"
          />

          <button
            type="submit"
            class="btn btn-primary d-flex align-items-center justify-content-center"
            :disabled="!question.trim() || isLoading"
            aria-label="Saada küsimus"
          >
            <PhPaperPlaneTilt :size="20" />
          </button>
        </form>
      </div>
    </div>

    <!-- Floating button -->
    <button
      v-motion
      :initial="{ scale: 0, opacity: 0 }"
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
      :aria-label="isOpen ? 'Sulge vestlus' : 'Ava vestlus'"
      @click="toggleChat"
    >
      <PhX v-if="isOpen" :size="30" />
      <PhChatCircleDots v-else :size="32" />
    </button>
  </div>
</template>

<style scoped>
.chatbot-widget {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1040;
}

.chatbot-button {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chatbot-panel {
  position: absolute;
  right: 0;
  bottom: 80px;
  width: min(390px, calc(100vw - 32px));
  height: min(540px, calc(100vh - 140px));
  overflow: hidden;
}

.chatbot-header {
  flex-shrink: 0;
}

.chatbot-messages {
  overflow-y: auto;
  flex: 1;
}

.chatbot-panel.card {
  display: flex;
  flex-direction: column;
}

.chatbot-message {
  max-width: 82%;
  padding: 10px 14px;
  border-radius: 16px;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.chatbot-message-user {
  background: var(--bs-primary);
  color: white;
  border-bottom-right-radius: 4px;
}

.chatbot-message-assistant {
  background: var(--bs-tertiary-bg);
  color: var(--bs-body-color);
  border-bottom-left-radius: 4px;
}

@media (max-width: 576px) {
  .chatbot-widget {
    right: 16px;
    bottom: 16px;
  }

  .chatbot-panel {
    position: fixed;
    left: 16px;
    right: 16px;
    bottom: 96px;
    width: auto;
    height: min(520px, calc(100vh - 130px));
  }
}
</style>
