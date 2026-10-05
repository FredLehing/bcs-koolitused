const fs = require('node:fs')
const path = require('node:path')
const assert = require('node:assert/strict')
const { test } = require('node:test')
const { parse, compileTemplate } = require('@vue/compiler-sfc')

const source = fs.readFileSync(path.join(__dirname, '../src/views/AdminFeedbacksView.vue'), 'utf8')
const descriptor = parse(source).descriptor
const script = descriptor.script.content
  .replace(/^import .*$/gm, '')
  .replace('export default', 'return')

function deferred() {
  let resolve, reject
  const promise = new Promise((a, b) => {
    resolve = a
    reject = b
  })
  return { promise, resolve, reject }
}
function view(admin = true) {
  const calls = { pages: [], details: [], reviews: [], courses: [] }
  const pending = { pages: [], details: [], reviews: [], courses: [] }
  const service = {}
  for (const [method, type] of Object.entries({
    sendGetAdminFeedbacksRequest: 'pages',
    sendGetAdminFeedbackRequest: 'details',
    sendPutAdminFeedbackReviewRequest: 'reviews',
    sendGetAdminFeedbackCoursesRequest: 'courses',
  })) {
    service[method] = (...args) => {
      calls[type].push(args)
      const request = deferred()
      pending[type].push(request)
      return request.promise
    }
  }
  let denied = false
  const environment = {
    mapState: () => ({}),
    useLanguageStore: () => {},
    AdminFeedbackService: service,
    SessionStorageService: { userIsAdmin: () => admin },
    NavigationService: {
      navigateToNotAuthorizedView: () => {
        denied = true
      },
    },
    FormatService: {},
    AdminTabs: {},
    PaginationNav: {},
    SortableColumnHeader: {},
    InlineAlerts: {},
  }
  const options = new Function(...Object.keys(environment), script)(...Object.values(environment))
  const routes = []
  const instance = {
    ...options.data(),
    contentLang: 'et',
    $t: (key) => key,
    $router: { push: (target) => routes.push(target) },
  }
  for (const [key, method] of Object.entries(options.methods)) instance[key] = method.bind(instance)
  for (const [key, computed] of Object.entries(options.computed))
    Object.defineProperty(instance, key, { get: () => computed.call(instance) })
  return { instance, options, calls, pending, routes, denied: () => denied }
}
const page = (ids = [7, 3], totalPages = 2) => ({
  data: { content: ids.map((feedbackId) => ({ feedbackId })), totalPages, totalElements: 9 },
})
const detail = (feedbackId = 3, status = 'U') => ({
  data: { feedbackId, status, answersVersion: 'a'.repeat(64), criteria: [] },
})
const tick = () => new Promise(setImmediate)

test('vaikeseis, admini kontroll ja template', () => {
  const { instance, options, calls, denied } = view(false)
  assert.equal(instance.limit, 15)
  assert.equal(instance.page, 0)
  assert.equal(instance.showFilters, false)
  options.beforeMount.call(instance)
  assert.equal(denied(), true)
  assert.equal(calls.pages.length, 0)
  const compiled = compileTemplate({
    source: descriptor.template.content,
    filename: 'AdminFeedbacksView.vue',
    id: 'test',
  })
  assert.deepEqual(compiled.errors, [])
})
test('mustand ja rakendatud filtrid, otsing, tühjendamine ning sort', async () => {
  const { instance: v, calls, pending } = view()
  v.draftFilters.courseId = 14
  v.searchText = '  Anna SQL  '
  v.applySearch()
  assert.equal(calls.pages.at(-1)[0].searchText, 'Anna SQL')
  assert.equal(calls.pages.at(-1)[0].courseId, undefined)
  pending.pages.at(-1).resolve(page())
  await tick()
  v.applyFilters()
  assert.equal(calls.pages.at(-1)[0].courseId, 14)
  pending.pages.at(-1).resolve(page())
  await tick()
  v.sort('averageScore')
  assert.equal(v.sortDirection, 'asc')
  pending.pages.at(-1).resolve(page())
  await tick()
  v.sort('averageScore')
  assert.equal(v.sortDirection, 'desc')
  pending.pages.at(-1).resolve(page())
  await tick()
  v.clearFilters()
  assert.equal(v.sortBy, 'averageScore')
  assert.equal(v.appliedSearch, '')
  assert.equal(v.hasFilters, false)
  pending.pages.at(-1).resolve(page())
  await tick()
  const count = calls.pages.length
  v.draftFilters.from = '2026-10-02'
  v.draftFilters.until = '2026-09-01'
  v.applyFilters()
  assert.equal(calls.pages.length, count)
  assert.ok(v.validationMessage)
})
test('aegunud tabeli ja detaili vastused ei kirjuta uut olekut üle', async () => {
  const { instance: v, pending } = view()
  const first = v.loadPage()
  const second = v.loadPage()
  pending.pages[1].resolve(page([3]))
  await second
  pending.pages[0].resolve(page([7]))
  await first
  assert.equal(v.pageData.content[0].feedbackId, 3)
  const oldDetail = v.loadDetail(3)
  const newDetail = v.loadDetail(7)
  pending.details[1].resolve(detail(7))
  await newDetail
  pending.details[0].resolve(detail(3))
  await oldDetail
  assert.equal(v.detail.feedbackId, 7)
  v.closeDetail()
  assert.equal(v.detail, null)
  assert.equal(v.openFeedbackId, null)
})
test('ülevaatus vajab edukat detaili, keelab topeltpäringu ja värskendab', async () => {
  const { instance: v, calls, pending } = view()
  const row = { feedbackId: 3 }
  v.review(row)
  assert.equal(calls.reviews.length, 0)
  const loading = v.loadDetail(3)
  pending.details[0].resolve(detail())
  await loading
  const reviewing = v.review(row)
  v.review(row)
  assert.equal(calls.reviews.length, 1)
  assert.deepEqual(calls.reviews[0], [3, 'a'.repeat(64)])
  pending.reviews[0].resolve({})
  await tick()
  pending.pages[0].resolve(page())
  await reviewing
  assert.equal(v.reviewing, false)
  assert.equal(v.openFeedbackId, null)
  assert.ok(v.successMessage)
})
test('409 avab viimased vastused ning ei korda PUT-i automaatselt', async () => {
  const { instance: v, calls, pending } = view()
  const loading = v.loadDetail(3)
  pending.details[0].resolve(detail())
  await loading
  const reviewing = v.review({ feedbackId: 3 })
  pending.reviews[0].reject({ response: { status: 409, data: { message: 'changed' } } })
  await tick()
  pending.details[1].resolve(detail(3, 'U'))
  pending.pages[0].resolve(page([3]))
  await reviewing
  assert.equal(calls.reviews.length, 1)
  assert.equal(v.detail.feedbackId, 3)
  assert.equal(v.errorMessage, 'changed')
})
test('tühi viimane leht korrigeeritakse; 404 ja võrguviga', async () => {
  const { instance: v, pending, routes } = view()
  v.page = 3
  const loading = v.loadPage()
  pending.pages[0].resolve(page([], 1))
  await tick()
  assert.equal(v.page, 0)
  pending.pages[1].resolve(page([3], 1))
  await loading
  const missing = v.loadDetail(3)
  pending.details[0].reject({ response: { status: 404, data: { message: 'missing' } } })
  await missing
  assert.equal(routes[0].name, 'errorRoute')
  assert.equal(v.canReview({ feedbackId: 3 }), false)
  const failed = v.loadPage()
  pending.pages[2].reject(new Error('network'))
  await failed
  assert.equal(v.pageData, null)
  assert.equal(v.errorMessage, 'adminFeedbacks.loadError')
})

test('keelevahetus säilitab rakendatud filtri, sordi, lehe ja avatud detaili', async () => {
  const { instance: v, options, calls, pending } = view()
  v.active = true
  v.appliedFilters.courseId = 14
  v.sortBy = 'averageScore'
  v.page = 1
  v.openFeedbackId = 3
  v.contentLang = 'en'
  options.watch.contentLang.call(v)
  assert.equal(calls.pages[0][0].contentLang, 'en')
  assert.equal(calls.pages[0][0].courseId, 14)
  assert.equal(calls.pages[0][0].sortBy, 'averageScore')
  assert.equal(calls.pages[0][0].page, 1)
  assert.deepEqual(calls.details[0], [3, 'en'])
  pending.pages[0].resolve(page([3]))
  pending.details[0].resolve(detail())
  pending.courses[0].resolve({ data: [] })
  await tick()
  assert.equal(v.openFeedbackId, 3)
  assert.equal(v.detail.feedbackId, 3)
})
