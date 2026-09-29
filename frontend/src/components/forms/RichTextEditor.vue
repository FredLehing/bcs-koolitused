<script>
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import {
  PhLink,
  PhLinkBreak,
  PhListBullets,
  PhListNumbers,
  PhTextB,
  PhTextItalic,
  PhTextUnderline,
} from '@phosphor-icons/vue'

// Lubatud vormingud peavad ühtima backendi HtmlSanitizer Safelist'iga:
// p, br, strong, em, u, h3, h4, ul, ol, li, a
export default {
  name: 'RichTextEditor',
  components: {
    EditorContent,
    PhLink,
    PhLinkBreak,
    PhListBullets,
    PhListNumbers,
    PhTextB,
    PhTextItalic,
    PhTextUnderline,
  },
  props: {
    html: String,
    labelId: String,
  },
  emits: ['event-new-html-input'],
  data() {
    return {
      editor: null,
      lastEmittedHtml: null,
    }
  },
  computed: {
    toolbarButtons() {
      return [
        { name: 'bold', icon: 'PhTextB', run: () => this.focusChain().toggleBold().run() },
        { name: 'italic', icon: 'PhTextItalic', run: () => this.focusChain().toggleItalic().run() },
        {
          name: 'underline',
          icon: 'PhTextUnderline',
          run: () => this.focusChain().toggleUnderline().run(),
        },
        {
          name: 'heading3',
          text: 'H3',
          active: ['heading', { level: 3 }],
          run: () => this.focusChain().toggleHeading({ level: 3 }).run(),
        },
        {
          name: 'heading4',
          text: 'H4',
          active: ['heading', { level: 4 }],
          run: () => this.focusChain().toggleHeading({ level: 4 }).run(),
        },
        {
          name: 'bulletList',
          icon: 'PhListBullets',
          run: () => this.focusChain().toggleBulletList().run(),
        },
        {
          name: 'orderedList',
          icon: 'PhListNumbers',
          run: () => this.focusChain().toggleOrderedList().run(),
        },
      ]
    },
  },
  watch: {
    // Väline sisu muutus (laadimine, oleku vahetus, AI tõlge) — oma emititud väärtust tagasi ei laeta
    html(newHtml) {
      if (newHtml === this.lastEmittedHtml) {
        return
      }
      this.lastEmittedHtml = null
      this.editor.commands.setContent(newHtml ?? '', { emitUpdate: false })
    },
  },
  methods: {
    focusChain() {
      return this.editor.chain().focus()
    },

    isButtonActive(button) {
      const [name, attributes] = button.active ?? [button.name]
      return this.editor.isActive(name, attributes)
    },

    handleLinkClick() {
      if (this.editor.isActive('link')) {
        this.focusChain().extendMarkRange('link').unsetLink().run()
        return
      }
      const url = window.prompt(this.$t('richTextEditor.linkPrompt'), 'https://')
      if (url === null || url.trim() === '' || url.trim() === 'https://') {
        return
      }
      this.focusChain()
        .extendMarkRange('link')
        .setLink({ href: this.addProtocolIfMissing(url.trim()) })
        .run()
    },

    addProtocolIfMissing(url) {
      return /^(https?:\/\/|mailto:)/i.test(url) ? url : 'https://' + url
    },

    // Kleebitud pealkirjad lubatud tasemele, muidu muutuksid need tavaliseks lõiguks
    transformPastedHtml(html) {
      return html
        .replace(/<(\/?)h[12](\s|>)/gi, '<$1h3$2')
        .replace(/<(\/?)h[56](\s|>)/gi, '<$1h4$2')
    },

    // Tühi editor annab '<p></p>' — vaatele saadetakse '', et vormi valideerimine töötaks
    emitHtml() {
      this.lastEmittedHtml = this.editor.isEmpty ? '' : this.editor.getHTML()
      this.$emit('event-new-html-input', this.lastEmittedHtml)
    },
  },
  mounted() {
    this.editor = new Editor({
      extensions: [
        StarterKit.configure({
          heading: { levels: [3, 4] },
          code: false,
          codeBlock: false,
          blockquote: false,
          horizontalRule: false,
          strike: false,
          link: {
            openOnClick: false,
            autolink: true,
            defaultProtocol: 'https',
            // lubatud ainult http(s) ja mailto — NB: `protocols` option lisab protokolle, ei piira
            isAllowedUri: (url, ctx) =>
              ctx.defaultValidate(url) && /^(https?:\/\/|mailto:)/i.test(url),
          },
        }),
      ],
      content: this.html ?? '',
      editorProps: {
        attributes: {
          class: 'rich-text-editor-content',
          'aria-labelledby': this.labelId,
        },
        transformPastedHTML: (html) => this.transformPastedHtml(html),
      },
      onUpdate: () => this.emitHtml(),
    })
  },
  beforeUnmount() {
    this.editor.destroy()
  },
}
</script>

<template>
  <div class="rich-text-editor">
    <div v-if="editor" class="btn-toolbar gap-1 p-1 border-bottom">
      <button
        v-for="button in toolbarButtons"
        :key="button.name"
        @click="button.run"
        :class="isButtonActive(button) ? 'btn-secondary' : 'btn-outline-secondary'"
        :title="$t('richTextEditor.' + button.name)"
        class="btn btn-sm"
        type="button"
      >
        <component :is="button.icon" v-if="button.icon" :size="16" weight="bold" />
        <span v-else class="fw-bold">{{ button.text }}</span>
      </button>
      <button
        @click="handleLinkClick"
        :class="editor.isActive('link') ? 'btn-secondary' : 'btn-outline-secondary'"
        :title="$t(editor.isActive('link') ? 'richTextEditor.unlink' : 'richTextEditor.link')"
        class="btn btn-sm"
        type="button"
      >
        <PhLinkBreak v-if="editor.isActive('link')" :size="16" weight="bold" />
        <PhLink v-else :size="16" weight="bold" />
      </button>
    </div>
    <EditorContent :editor="editor" />
  </div>
</template>

<style scoped>
.rich-text-editor {
  border: var(--bs-border-width) solid var(--bs-border-color);
  border-radius: var(--bs-border-radius);
  background-color: var(--bs-body-bg);
}

.rich-text-editor:focus-within {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}

.rich-text-editor :deep(.rich-text-editor-content) {
  min-height: 200px;
  padding: 0.375rem 0.75rem;
  outline: none;
}

/* TipTap paneb iga loendi elemendi sisse <p> — Bootstrapi margin teeks loendisse suured vahed */
.rich-text-editor :deep(li > p) {
  margin-bottom: 0;
}

/* Kirjelduse alapealkirjad, mitte lehe pealkirjad */
.rich-text-editor :deep(h3) {
  font-size: 1.25rem;
  margin-top: 0.75rem;
}

.rich-text-editor :deep(h4) {
  font-size: 1rem;
  font-weight: bold;
  margin-top: 0.75rem;
}
</style>
