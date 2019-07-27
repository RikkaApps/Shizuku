module.exports = {
  base: '/',
  title: 'Shizuku',
  head: [
    ['link', {
      rel: 'stylesheet',
      href: 'https://fonts.googleapis.com/css?family=Roboto:400,400i,500,500i,700,700i&display=swap'
    }],
    ['link', {
      rel: 'stylesheet',
      href: 'https://fonts.googleapis.com/css?family=Noto+Sans+SC:400,500,700&display=swap'
    }],
    ['link', {
      rel: 'stylesheet',
      href: 'https://fonts.googleapis.com/css?family=Noto+Sans+TC:400,500,700&display=swap'
    }],
    ['link', { rel: 'apple-touch-icon', size: '57x57', href: '/icon/apple-icon-57x57.png' }],
    ['link', { rel: 'apple-touch-icon', size: '60x60', href: '/icon/apple-icon-60x60.png' }],
    ['link', { rel: 'apple-touch-icon', size: '72x72', href: '/icon/apple-icon-72x72.png' }],
    ['link', { rel: 'apple-touch-icon', size: '76x76', href: '/icon/apple-icon-76x76.png' }],
    ['link', { rel: 'apple-touch-icon', size: '114x114', href: '/icon/apple-icon-114x114.png' }],
    ['link', { rel: 'apple-touch-icon', size: '120x120', href: '/icon/apple-icon-120x120.png' }],
    ['link', { rel: 'apple-touch-icon', size: '144x144', href: '/icon/apple-icon-144x144.png' }],
    ['link', { rel: 'apple-touch-icon', size: '152x152', href: '/icon/apple-icon-152x152.png' }],
    ['link', { rel: 'apple-touch-icon', size: '180x180', href: '/icon/apple-icon-180x180.png' }],
    ['link', { rel: 'icon', type: 'image/png', size: '192x192', href: '/icon/android-icon-192x192.png' }],
    ['link', { rel: 'icon', type: 'image/png', size: '32x32', href: '/icon/favicon-32x32.png' }],
    ['link', { rel: 'icon', type: 'image/png', size: '96x96', href: '/icon/favicon-96x96.png' }],
    ['link', { rel: 'icon', type: 'image/png', size: '16x16', href: '/icon/favicon-16x16.png' }]
  ],
  locales: {
    '/': {
      lang: 'en',
      description: 'Let your app use system APIs directly'
    },
    '/zh-hans/': {
      lang: 'zh-Hans',
      description: '让你的应用直接使用系统 API'
    },
    '/zh-hant/': {
      lang: 'zh-Hant',
      description: '讓你的應用程式直接使用系統 API'
    }
  },
  themeConfig: {
    locales: {
      '/': {
        selectText: 'Languages',
        label: 'English',
        serviceWorker: {
          updatePopup: {
            message: "New content is available.",
            buttonText: "Refresh"
          }
        },
        sidebar: {
          '/guide/': getSidebar()
        },
        nav: getNavbar('/', 'Guide', 'Who\'s using', 'Download'),
        lastUpdated: 'Last Updated'
      }
      ,
      '/zh-hans/': {
        selectText: '语言',
        label: '简体中文',
        editLinkText: '在 GitHub 上编辑此页',
        serviceWorker: {
          updatePopup: {
            message: "发现新内容可用.",
            buttonText: "刷新"
          }
        },
        sidebar: {
          '/zh-hans/guide/': getSidebar()
        },
        nav: getNavbar('/zh-hans/', '指南', '谁在用', '下载'),
        lastUpdated: '最后更新'
      },
      '/zh-hant/': {
        selectText: '語言',
        label: '繁體中文',
        editLinkText: '在 GitHub 上編輯此頁',
        serviceWorker: {
          updatePopup: {
            message: "發現新內容可用.",
            buttonText: "重新整理"
          }
        },
        sidebar: {
          '/zh-hant/guide/': getSidebar()
        },
        nav: getNavbar('/zh-hant/', '指南', '誰在用', '下載'),
        lastUpdated: '最後更新'
      }
    },
    displayAllHeaders: true,
    sidebarDepth: 2,
    serviceWorker: {
      updatePopup: true
    },
    repo: 'https://github.com/RikkaApps/Shizuku',
    docsDir: 'docs',
    editLinks: true
  }
}

function getSidebar() {
  return ['', 'setup', 'dev']
}

function getNavbar(lang, guide, apps, download) {
  var prefix = lang == '/en' ? '' : lang
  return [
    { text: guide, link: `${prefix}guide/` },
    { text: apps, link: `${prefix}apps.html` },
    { text: download, link: `${prefix}download.html` },
  ]
}