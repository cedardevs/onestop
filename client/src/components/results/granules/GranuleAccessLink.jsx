import React, {useEffect, useRef, useState} from 'react'

// import {isGovExternal} from '../../../utils/urlUtils'
import * as util from '../../../utils/resultUtils'
import defaultStyles from '../../../style/defaultStyles'
import A from '../../common/link/Link'
// import Button from '../../common/input/Button'

const styleBadgeLink = {
  textDecoration: 'none',
  display: 'inline-flex',
  paddingRight: '0.309em',
}

const styleBadgeLinkFocused = {
  outline: '2px dashed white',
  outlineOffset: '0.105em',
}
/*
  // TODO this is the video link thing - need to go relocate my test data for this and make it work with hooks
   renderBadge = link => {
     ...

     let focusRef = null
     const allowVideo =
       linkProtocol === 'video:youtube' ||
       (url.includes('.mp4') && !isGovExternal(url))
     const videoPlay =
       protocol.label === 'Video' && allowVideo ? (
         <Button
           key={`video-play-button-${url}`}
           styleHover={styleHoverPlayButton}
           style={stylePlayButton}
           ref={ref => {
             focusRef = ref
           }}
           onClick={() => {
             this.props.showGranuleVideo(this.props.itemId)
             this.setState(prevState => {
               return {
                 ...prevState,
                 videoPlaying: {
                   protocol: linkProtocol,
                   url: url,
                   returnFocusRef: focusRef,
                 },
               }
             })
           }}
           title={`Play ${linkText}`}
         >
           <SvgIcon size="1em" path={play_circle_o} />
         </Button>
       ) : null
     ...
   }
*/
const GranuleAccessLink = props => {
  const {link, item, itemId} = props
  const {protocol, url, displayName, linkProtocol} = link
  const linkText = displayName ? displayName : protocol.label
  const accessibleProtocolText = displayName
    ? `protocol: ${protocol.label} for ${item.title}`
    : ` for ${item.title}` // prevent duplicate reading of protocol.label if that is also used as the linkText
  const videoPlay = null // TODO!!!
  return (
    <li key={`accessLink::${url}`} style={util.styleProtocolListItem}>
      <div
        title={protocol.label}
        style={util.styleBadge(protocol)}
        aria-hidden="true"
      >
        {util.renderBadgeIcon(protocol)}
      </div>
      <A
        href={url}
        key={url}
        target="_blank"
        style={styleBadgeLink}
        styleFocus={styleBadgeLinkFocused}
      >
        <div
          id={`ListResult::Link::${url}`}
          style={{
            ...{
              textDecoration: 'underline',
              margin: '0.6em 0',
            },
          }}
        >
          {linkText}{' '}
          <span style={defaultStyles.hideOffscreen}>
            {accessibleProtocolText}
          </span>
        </div>
      </A>
      {videoPlay}
    </li>
  )
}
export default GranuleAccessLink
