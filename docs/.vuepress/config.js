module.exports = {
  base: '/shizuku/',
  title: 'Shizuku',
  locales: {
    '/': {
      lang: 'en',
    },
    '/zh-hans/': {
      lang: 'zh-Hans',
    },
    '/zh-hant/': {
      lang: 'zh-Hant',
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
          '/': getSidebar(true)
        },
        lastUpdated: 'Last Updated'
      },
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
          '/zh-hans/': getSidebar()
        },
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
          '/zh-hant/': getSidebar()
        },
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

function getSidebar(isEnglish = false) {
  return isEnglish ? ['', 'en/setup', 'en/apps', 'en/dev'] : ['', 'setup', 'apps', 'dev']
}