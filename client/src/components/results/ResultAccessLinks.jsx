import React from 'react'
import * as util from '../../utils/resultUtils'
import GranuleAccessLink from './granules/GranuleAccessLink'

export default function ResultAccessLinks(props){
  const {itemId, item, handleShowGranuleVideo} = props

  const badges = _.chain(item.links)
    .map(link => ({
      protocol: util.identifyProtocol(link),
      url: link.linkUrl,
      displayName: link.linkName
        ? link.linkName
        : link.linkDescription ? link.linkDescription : null,
      linkProtocol: link.linkProtocol, // needed to handle videos consistently
    }))
    .sortBy(info => info.protocol.id)
    .map((link, index) => {
      return (
        <GranuleAccessLink
          key={`accessLink::${itemId}::${index}`}
          link={link}
          item={item}
          itemId={itemId}
          showGranuleVideo={handleShowGranuleVideo}
        />
      )
    })
    .value()
  const badgesElement = _.isEmpty(badges) ? 'N/A' : badges

  return <ul style={util.styleProtocolList}>{badgesElement}</ul>
}
