
// makes urls protocol-relative and url-encodes quotes
export const processUrl = (url) => {
  if (typeof url === 'string') {
    return url.replace(/^https?:/, '').replace(/'/, '%27').replace(/"/, '%22')
  }
  else {
    return url
  }
}
