<script>
import DOMPurify from 'dompurify'

// Richtext (HTML) kirjelduse kuvamine. Backend puhastab HTML-i salvestamisel (HtmlSanitizer),
// DOMPurify on teine kaitsekiht enne v-html-i. Lubatud märgendid ühtivad RichTextEditor.vue-ga.
const ALLOWED_TAGS = ['p', 'br', 'strong', 'em', 'u', 'h3', 'h4', 'ul', 'ol', 'li', 'a']
const ALLOWED_ATTR = ['href', 'target', 'rel']

export default {
  name: 'RichTextContent',
  props: {
    html: String,
  },
  computed: {
    sanitizedHtml() {
      return DOMPurify.sanitize(this.html ?? '', { ALLOWED_TAGS, ALLOWED_ATTR })
    },
  },
}
</script>

<template>
  <div class="rich-text-content" v-html="sanitizedHtml"></div>
</template>

<style scoped>
/* v-html sisu ei saa scoped stiile — seega :deep() */

/* TipTap paneb iga loendi elemendi sisse <p> — Bootstrapi margin teeks loendisse suured vahed */
.rich-text-content :deep(li > p) {
  margin-bottom: 0;
}

/* Kirjelduse alapealkirjad, mitte lehe pealkirjad (samad suurused mis editoris) */
.rich-text-content :deep(h3) {
  font-size: 1.25rem;
  margin-top: 0.75rem;
}

.rich-text-content :deep(h4) {
  font-size: 1rem;
  font-weight: bold;
  margin-top: 0.75rem;
}
</style>
