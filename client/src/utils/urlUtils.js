import _ from 'lodash'

export const getBasePath = () => {
  const windowpath = window.location.pathname
  return extractBaseFromKnownRoutes(windowpath) || windowpath
}

export const apiPath = () => {
  return getBasePath().replace(/\/$/, '') + '-search'
}

// makes urls protocol-relative and url-encodes quotes
export const processUrl = url => {
  if (typeof url === 'string') {
    return url.replace(/^https?:/, '').replace(/'/, '%27').replace(/"/, '%22')
  }
  else {
    return url
  }
}

export const govExternalPopupMsg = `The site you are navigating to is not hosted by the US Government.

Thank you for visiting our site. We have provided \
this link because it has information that may interest you, but we do not \
endorse the views expressed, the information presented, or any commercial \
products that may be advertised or available on that site.`

export const govExternalYouTubeMsg = `Videos on YouTube are not hosted by the US Government.
We have provided this link because it has information that may interest you, but we do not \
endorse the views expressed, the information presented, or any commercial products that may \
be advertised or available on that site.`

// returns true of the url points outside the .gov domain
export const isGovExternal = url => {
  const isAbsolute = url.match(/^([a-zA-Z]+:)?\/\//) !== null
  const isGov = url.match(/^([a-zA-Z]+:)?\/\/[^/]+\.gov/) !== null
  return isAbsolute && !isGov
}

export const buildGovExternalOnClick = (href, target, onClick) => {
  return e => {
    if (typeof onClick === 'function') {
      onClick()
    }

    if (isGovExternal(href)) {
      e.preventDefault()
      if (window.confirm(govExternalPopupMsg)) {
        target ? window.open(href, target) : (window.location.href = href)
      }
    }
  }
}

export const ROUTE = Object.freeze({
  sitemap: {
    path: '/sitemap.xml',
    regex: /\/sitemap.xml/,
  },
  search: {
    path: '/collections',
    regex: /\/collections([^/])*$/,
  },
  cart: {
    path: '/cart',
    regex: /\/cart/,
  },
  details: {
    path: '/collections/details',
    regex: /\/collections\/details\/([-\w]+)/,
  },
  granules: {
    path: '/collections/granules',
    parameterized: '/collections/granules/:id',
    regex: /\/collections\/granules\/([-\w]+)/,
  },
  about: {path: '/about', regex: /\/about/},
  help: {path: '/help', regex: /\/help/},
  error: {path: '/error', regex: /\/error/},
})

export const isRoute = (path, route) => {
  return route.regex.exec(path)
}

export const extractBaseFromKnownRoutes = path => {
  var findMatch = _.find(ROUTE, route => {
    return isRoute(path, route)
  })
  if (findMatch) {
    var re = new RegExp(findMatch.path + '.*')
    return path.replace(re, '/')
  }
}

const sitemapMatch = path => {
  return isRoute(path, ROUTE.sitemap)
}

export const isSitemap = path => {
  return sitemapMatch(path) ? true : false
}

export const isDetailPage = path => {
  return isRoute(path, ROUTE.details) ? true : false
}

export const isGranuleListPage = path => {
  return isRoute(path, ROUTE.granules) ? true : false
}

export const getCollectionIdFromDetailPath = path => {
  if (!isDetailPage(path)) {
    return null
  }
  const match = isRoute(path, ROUTE.details)
  return match && match[1] ? match[1] : null
}

export const getCollectionIdFromGranuleListPath = path => {
  if (!isGranuleListPage(path)) {
    return null
  }
  const match = isRoute(path, ROUTE.granules)
  return match && match[1] ? match[1] : null
}
