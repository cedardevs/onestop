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
