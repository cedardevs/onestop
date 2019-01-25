import _ from 'lodash'

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
