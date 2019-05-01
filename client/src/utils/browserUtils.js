export const browserUnsupported = () => {
  let unsupported = false

  // check for flex support
  const flexSupport =
    document.body.style.flex !== undefined &&
    document.body.style.flexFlow !== undefined
  if (!flexSupport) {
    unsupported = true
  }

  return unsupported
}
