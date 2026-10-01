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
    isDisabled: Boolean,
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
    isDisabled(isDisabled) {
      this.editor?.setEditable(!isDisabled)
    },
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
      editable: !this.isDisabled,
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
  <div
    :class="isDisabled ? 'bg-slate-100 text-muted' : 'bg-white'"
    class="overflow-hidden rounded-lg border border-brand-200 focus-within:border-brand-600 focus-within:ring-3 focus-within:ring-brand-600/15"
  >
    <div v-if="editor" class="flex flex-wrap gap-1 border-b border-line bg-surface p-1.5">
      <button
        v-for="button in toolbarButtons"
        :key="button.name"
        @click="button.run"
        :class="{ 'border-brand-600 bg-brand-50 text-brand-700': isButtonActive(button) }"
        :title="$t('richTextEditor.' + button.name)"
        :aria-label="$t('richTextEditor.' + button.name)"
        :aria-pressed="isButtonActive(button)"
        class="btn btn-outline-secondary btn-sm btn-icon"
        type="button"
        :disabled="isDisabled"
      >
        <component :is="button.icon" v-if="button.icon" :size="16" weight="bold" />
        <span v-else class="font-bold">{{ button.text }}</span>
      </button>
      <button
        @click="handleLinkClick"
        :class="{ 'border-brand-600 bg-brand-50 text-brand-700': editor.isActive('link') }"
        :title="$t(editor.isActive('link') ? 'richTextEditor.unlink' : 'richTextEditor.link')"
        :aria-label="$t(editor.isActive('link') ? 'richTextEditor.unlink' : 'richTextEditor.link')"
        class="btn btn-outline-secondary btn-sm btn-icon"
        type="button"
        :disabled="isDisabled"
      >
        <PhLinkBreak v-if="editor.isActive('link')" :size="16" weight="bold" />
        <PhLink v-else :size="16" weight="bold" />
      </button>
    </div>
    <EditorContent :editor="editor" />
  </div>
</template>

<style scoped>
/* Redaktori sisu (.ProseMirror) luuakse TipTapi poolt — seega :deep(). Tailwindi baasstiil eemaldab
   vahed ja loendimärgid; need on samad mis kuvamisel (RichTextContent.vue), et sisu näeks välja nagu lehel. */
:deep(.rich-text-editor-content) {
  min-height: 200px;
  padding: 0.625rem 0.75rem;
  line-height: 1.65;
  outline: none;
}

:deep(.rich-text-editor-content p),
:deep(.rich-text-editor-content ul),
:deep(.rich-text-editor-content ol) {
  margin-bottom: 0.875rem;
}

:deep(.rich-text-editor-content ul) {
  list-style: disc;
  padding-left: 1.4rem;
}

:deep(.rich-text-editor-content ol) {
  list-style: decimal;
  padding-left: 1.4rem;
}

/* TipTap paneb iga loendi elemendi sisse <p> */
:deep(.rich-text-editor-content li > p) {
  margin-bottom: 0;
}

:deep(.rich-text-editor-content a) {
  color: var(--color-brand-600);
  text-decoration: underline;
}

/* Kirjelduse alapealkirjad, mitte lehe pealkirjad */
:deep(.rich-text-editor-content h3) {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 1.25rem 0 0.5rem;
}

:deep(.rich-text-editor-content h4) {
  font-size: 1rem;
  font-weight: 700;
  margin: 1rem 0 0.375rem;
}

:deep(.rich-text-editor-content > :first-child) {
  margin-top: 0;
}
</style>
