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
/* v-html sisu ei saa scoped stiile — seega :deep(). Tailwindi baasstiil eemaldab vahed ja loendimärgid,
   kirjelduse tekst vajab need tagasi. */
.rich-text-content {
  line-height: 1.65;
}

.rich-text-content :deep(p),
.rich-text-content :deep(ul),
.rich-text-content :deep(ol) {
  margin-bottom: 0.875rem;
}

.rich-text-content :deep(ul) {
  list-style: disc;
  padding-left: 1.4rem;
}

.rich-text-content :deep(ol) {
  list-style: decimal;
  padding-left: 1.4rem;
}

/* TipTap paneb iga loendi elemendi sisse <p> */
.rich-text-content :deep(li > p) {
  margin-bottom: 0;
}

.rich-text-content :deep(a) {
  text-decoration: underline;
}

/* Kirjelduse alapealkirjad, mitte lehe pealkirjad (samad suurused mis editoris) */
.rich-text-content :deep(h3) {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 1.25rem 0 0.5rem;
}

.rich-text-content :deep(h4) {
  font-size: 1rem;
  font-weight: 700;
  margin: 1rem 0 0.375rem;
}
</style>
