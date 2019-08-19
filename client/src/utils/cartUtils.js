import _ from 'lodash'

// if a user tries to add more than this number of items to the cart at one time,
// they will be shown an error and the items won't be added to the cart
// 1,000 items is used here because that is already the limit of a single search API granule request
export const MAX_CART_ADDITION = 1000

// if a user tries to add to the cart and the new cart size would exceed this number,
// they will be shown an error and the items won't be added to the cart
// 10,000 items is used here because that is a generous constraint on a UI, redux store, and local storage
export const CART_CAPACITY = 10000

export const granuleDownloadableLinks = (granules, protocol, source) => {
  let downloadLinks = []
  granules.forEach(granule => {
    const selectedGranuleLinks = granule.links
    const matchingLink = selectedGranuleLinks.find(link => {
      // if protocol or source are not specified, they don't factor into the check
      const protocolCheck = protocol ? link.linkProtocol === protocol : true
      const sourceCheck = source ? link.linkName === source : true
      return (
        protocolCheck &&
        sourceCheck && // linkName ~ "source"
        link.linkFunction === 'download' &&
        link.linkUrl
      )
    })
    if (matchingLink) {
      downloadLinks.push(matchingLink.linkUrl)
    }
  })
  return _.uniq(downloadLinks)
}
