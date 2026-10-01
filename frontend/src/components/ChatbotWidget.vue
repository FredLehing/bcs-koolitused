<script>
import { PhChatCircleDots, PhX } from '@phosphor-icons/vue'
import rainImage from '@/assets/1024x1024rain.png'
import ChatbotService from '@/api-services/ChatbotService.js'
import { useLanguageStore } from '@/stores/languageStore.js'

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
      rainImage,
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

        const response = await ChatbotService.sendQuestionRequest(
          question,
          languageStore.contentLang,
        )

        this.messages.push({
          role: 'assistant',
          text: response.data.answer,
        })
      } catch (error) {
        this.errorMessage = error.response?.data?.message || this.$t('chatbot.error')
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
    <div
      v-if="isOpen"
      v-motion
      :initial="{
        opacity: 0,
        y: 70,
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
      class="rain-chatbot"
    >
      <img :src="rainImage" class="rain-image" alt="Rain" />

      <div class="rain-monitor">
        <div ref="messages" class="rain-messages">
          <div
            v-for="(message, index) in messages"
            :key="index"
            class="rain-message"
            :class="message.role === 'user' ? 'rain-message-user' : 'rain-message-assistant'"
          >
            {{ message.text }}
          </div>

          <div v-if="isLoading" class="rain-loading">
            {{ $t('chatbot.loading') }}
          </div>
        </div>

        <div v-if="errorMessage" class="rain-error">
          {{ errorMessage }}
        </div>

        <form class="rain-input" @submit.prevent="sendQuestion">
          <input
            v-model="question"
            type="text"
            maxlength="500"
            :placeholder="$t('chatbot.placeholder')"
            :disabled="isLoading"
            autocomplete="off"
          />
        </form>
      </div>
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
      <PhX v-if="isOpen" :size="30" />

      <PhChatCircleDots v-else :size="32" />
    </button>
  </div>
</template>

<style scoped>
.chatbot-widget {
  position: fixed;
  right: 24px;
  bottom: 0;
  z-index: 1040;
}

/*
  Rain oli enne 430 px.
  645 px = umbes 1.5 × suurem.
*/
.rain-chatbot {
  position: absolute;
  right: 20px;
  bottom: -18px;
  width: min(645px, calc(100vw - 90px));
  pointer-events: none;
}

.rain-image {
  display: block;
  width: 100%;
  height: auto;
}

/*
  Monitori ala liigub koos pildiga,
  kuna mõõdud on protsentides.
*/
.rain-monitor {
  position: absolute;
  top: 54%;
  right: 21.5%;
  bottom: 10.5%;
  left: 15.5%;

  display: flex;
  flex-direction: column;

  overflow: hidden;
  border-radius: 4px;

  color: var(--bs-body-color);
  pointer-events: auto;
}

/*
  Vastused võivad endiselt scrollida,
  kuid scrollbar ise ei ole nähtav.
*/
.rain-messages {
  flex: 1;
  min-height: 0;

  overflow-y: auto;
  overflow-x: hidden;

  padding: 4% 5% 2%;

  font-size: clamp(0.72rem, 1.25vw, 0.9rem);

  scrollbar-width: none;
  -ms-overflow-style: none;
}

.rain-messages::-webkit-scrollbar {
  display: none;
}

.rain-message {
  width: fit-content;
  max-width: 94%;

  margin-bottom: 3%;
  padding: 2.5% 3.5%;

  border-radius: 9px;

  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.rain-message-user {
  margin-left: auto;

  background: var(--bs-primary);
  color: white;
}

.rain-message-assistant {
  background: rgba(245, 240, 210, 0.92);
}

.rain-loading {
  padding: 0 5% 2%;

  font-size: clamp(0.66rem, 1.1vw, 0.82rem);
}

.rain-error {
  padding: 2% 5%;

  color: var(--bs-danger);

  font-size: clamp(0.64rem, 1vw, 0.8rem);
}

/*
  Ainult väike sisestusväli.
  Eraldi send-nuppu enam ei ole.
*/
.rain-input {
  display: block;

  padding: 2% 5% 3%;

  background: transparent;
}

.rain-input input {
  display: block;

  width: 100%;
  height: 30px;

  border: 1px solid rgba(110, 105, 80, 0.28);

  border-radius: 6px;

  padding: 0 10px;

  background: rgba(255, 255, 255, 0.88);

  color: var(--bs-body-color);

  font-size: clamp(0.64rem, 1vw, 0.8rem);

  outline: none;
}

.rain-input input:focus {
  border-color: rgba(13, 110, 253, 0.55);
}

.rain-input input:disabled {
  opacity: 0.7;
}

/*
  Chatbot avamise/sulgemise nupp jääb alles.
*/
.chatbot-button {
  position: relative;

  z-index: 2;

  width: 64px;
  height: 64px;

  margin-bottom: 16px;

  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;
}

/*
  Väiksel ekraanil ei saa Rain olla 645 px,
  seega skaleerub viewporti järgi.
*/
@media (max-width: 700px) {
  .chatbot-widget {
    right: 12px;
    bottom: 0;
  }

  .rain-chatbot {
    right: 8px;
    bottom: -10px;

    width: min(540px, calc(100vw - 40px));
  }

  .chatbot-button {
    width: 58px;
    height: 58px;
    margin-bottom: 12px;
  }
}
</style>
