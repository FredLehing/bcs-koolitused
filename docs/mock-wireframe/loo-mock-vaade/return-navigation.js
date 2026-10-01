// Läbimängude ühine tagasitee. Sama kokkulepe nagu BackLink.vue ja NavigationService.
(function () {
  'use strict'
  var root = new URL('.', document.currentScript.src)
  var defaults = {
    '/training': '/trainings', '/course': '/courses', '/lecturer': '/lecturers',
    '/training-form': '/admin-trainings', '/lecturer-form': '/admin-lecturers',
    '/room-form': '/admin-rooms', '/admin-training-courses': '/admin-trainings',
    '/course-form': '/admin-all-courses', '/admin-course': '/admin-all-courses',
    '/admin-enquiry': '/admin-enquiries', '/admin-registration': '/admin-registrations',
    '/admin-user': '/admin-users', '/course-registration': '/courses',
  }
  var known = Object.keys(defaults).concat([
    '/', '/trainings', '/courses', '/lecturers', '/admin-trainings', '/admin-lecturers',
    '/admin-rooms', '/admin-all-courses', '/admin-enquiries', '/admin-registrations',
    '/admin-users', '/participant-courses', '/participant-details', '/participant-certificates',
    '/change-password', '/login', '/signup', '/error', '/not-authorized', '/test',
  ])
  var shellPath = new URLSearchParams(location.search).get('appPath') || ''
  var lastPath = ''

  function internal(path) {
    return typeof path === 'string' && path[0] === '/' && path[1] !== '/' &&
      !/[\\\u0000-\u001f\u007f]/.test(path) && known.indexOf(new URL(path, root).pathname) !== -1
  }
  function fullPath(url) { return url.pathname + url.search + url.hash }
  function identity(path) {
    var url = new URL(path, root)
    return url.pathname + ['trainingId', 'courseId', 'lecturerId', 'enquiryId',
      'courseParticipantId', 'userId', 'roomId'].map(function (key) {
      return '|' + key + '=' + (url.searchParams.get(key) || '')
    }).join('')
  }
  function returnTo(path) {
    if (!internal(path)) return ''
    var query = new URL(path, root).searchParams
    var values = query.getAll('returnTo')
    var value = values.length === 1 ? values[0] : ''
    return internal(value) && identity(value) !== identity(path) ? value : ''
  }
  function currentPath() {
    if (lastPath) return lastPath
    if (internal(shellPath)) return shellPath
    var bar = document.getElementById('urlbar')
    if (!bar) return '/'
    try { return fullPath(new URL(bar.textContent, root)) } catch (error) { return '/' }
  }
  function forward(path) {
    if (!internal(path)) return path
    var source = currentPath()
    var previous = returnTo(source)
    if (previous && identity(path) === identity(previous)) return previous
    var target = new URL(path, root)
    if (target.pathname === '/login' && (target.searchParams.get('redirect') || '').startsWith('/course-registration?')) {
      target.searchParams.set('redirect', forward(target.searchParams.get('redirect')))
      return fullPath(target)
    }
    if (defaults[target.pathname] && identity(source) !== identity(path)) {
      target.searchParams.set('returnTo', source)
    }
    return fullPath(target)
  }
  function getBack(fallback) {
    var source = currentPath(), url = new URL(source, root)
    if (returnTo(source)) return returnTo(source)
    if (fallback) return fallback
    if (url.pathname === '/course-registration') return '/course?courseId=' + url.searchParams.get('courseId')
    if ((url.pathname === '/course-form' || url.pathname === '/admin-course') && url.searchParams.has('trainingId')) {
      return '/admin-training-courses?trainingId=' + url.searchParams.get('trainingId')
    }
    return defaults[url.pathname] || '/'
  }
  function render(goRoute, fallback) {
    var bar = document.getElementById('urlbar')
    if (!bar) return
    var path
    try { path = fullPath(new URL(bar.textContent, root)) } catch (error) { return }
    if (!internal(path)) return
    var url = new URL(path, root)
    if (lastPath) {
      var old = new URL(lastPath, root), previous = returnTo(lastPath)
      if (previous && identity(path) === identity(previous)) path = previous
      else if (url.pathname === old.pathname && (identity(path) === identity(lastPath) || /-form$/.test(url.pathname))) {
        if (previous) { url.searchParams.set('returnTo', previous); path = fullPath(url) }
      } else if (defaults[url.pathname]) path = forward(path)
    } else if (internal(shellPath) && identity(path) === identity(shellPath)) path = shellPath
    lastPath = path
    bar.textContent = 'https://bcskoolitus.ee' + path
    var area = document.getElementById('view') || document.getElementById('form')
    if (!area || !defaults[new URL(path, root).pathname]) return
    var existing = area.querySelector('[data-context-back]')
    if (existing) existing.remove()
    var oldBack = area.querySelector('#b-back')
    if (oldBack && /^←/.test(oldBack.textContent)) oldBack.remove()
    var link = document.createElement('a')
    var destination = getBack(fallback)
    link.dataset.contextBack = '1'
    link.textContent = '← Tagasi'
    link.className = 'linkish'
    link.style.justifySelf = 'start'
    link.style.marginBottom = '8px'
    link.href = new URL('index.html?path=' + encodeURIComponent(destination), root).href
    link.onclick = function (event) {
      if (event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return
      event.preventDefault()
      goRoute(destination, 'Tagasi → ' + destination, true)
    }
    area.prepend(link)
  }
  window.BcsReturnNavigation = { forward: forward, render: render, getBack: getBack, returnTo: returnTo, currentPath: currentPath }
})()
