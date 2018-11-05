const rootUrl = `${window.location.origin + window.location.pathname}`

const ogUrlMetaTag = document.createElement('meta')
ogUrlMetaTag.setAttribute('property', 'og:url')
ogUrlMetaTag.setAttribute('content', `${rootUrl}`)
document.head.appendChild(ogUrlMetaTag)
