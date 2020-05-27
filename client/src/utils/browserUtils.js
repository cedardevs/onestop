import {detect} from 'detect-browser'

export const isBrowserUnsupported = () => {
  let unsupported = false

  // check for flex support
  const flexSupport =
    document.body.style.flex !== undefined &&
    document.body.style.flexFlow !== undefined
  if (!flexSupport) {
    unsupported = true
  }

  const browser = detect()

  if (!browser) {
    console.log('browser not detected')
  }
  else if (browser.name.toLowerCase() == 'ie') {
    unsupported = true
  }

  return unsupported
}
