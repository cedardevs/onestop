
// makes urls protocol-relative and url-encodes quotes
export const processUrl = (url) => {
  if (typeof url === 'string') {
    return url.replace(/^https?:/, '').replace(/'/, '%27').replace(/"/, '%22')
  }
  else {
    return url
  }
}

// returns true of the url points outside the .gov domain
export const isGovExternal = (url) => {
  const isAbsolute = url.match(/^([a-zA-Z]+:)?\/\//) !== null
  const isGov = url.match(/^([a-zA-Z]+:)?\/\/[^/]+\.gov/) !== null
  return isAbsolute && !isGov
}
