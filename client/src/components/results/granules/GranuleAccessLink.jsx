import React, {useEffect, useRef, useState} from 'react'

import {isGovExternal} from '../../../utils/urlUtils'
import * as util from '../../../utils/resultUtils'
import {renderBadgeIcon} from '../../../utils/resultComponentUtils'
import defaultStyles from '../../../style/defaultStyles'
import A from '../../common/link/Link'
import Button from '../../common/input/Button'
import {play_circle_o, SvgIcon} from '../../common/SvgIcon'

const styleBadgeLink = {
  textDecoration: 'none',
  display: 'inline-flex',
  paddingRight: '0.309em',
}

const styleBadgeLinkFocused = {
  outline: '2px dashed black',
  outlineOffset: '0.105em',
}

const stylePlayButton = {
  alignSelf: 'center',
  background: 'none',
  border: 'none',
  outline: 'none',
  padding: '0.309',
}

const styleHoverPlayButton = {
  background: 'none',
  fill: 'blue',
}

const videoPlayButton = (
  linkText,
  linkProtocol,
  url,
  videoRef,
  playFunction
) => {
  return (
    <Button
      key={`video-play-button-${url}`}
      styleHover={styleHoverPlayButton}
      style={stylePlayButton}
      ref={videoRef}
      onClick={playFunction}
      title={`Play ${linkText}`}
    >
      <SvgIcon size="1em" path={play_circle_o} />
    </Button>
  )
}

const GranuleAccessLink = props => {
  const {link, item, itemId} = props
  const {protocol, url, displayName, linkProtocol} = link
  const linkText = displayName ? displayName : linkProtocol
  const accessibleProtocolText = displayName
    ? `protocol: ${protocol.label} for ${item.title}`
    : ` for ${item.title}` // prevent duplicate reading of protocol.label if that is also used as the linkText
  const videoRef = useRef(null)

  const allowVideo =
    (linkProtocol === 'video:youtube' ||
      (url.includes('.mp4') && !isGovExternal(url))) &&
    props.showGranuleVideo
  const videoPlay =
    protocol.label === 'Video' && allowVideo
      ? videoPlayButton(linkText, linkProtocol, url, videoRef, () => {
          props.showGranuleVideo(linkProtocol, url, videoRef.current, itemId)
        })
      : null

  return (
    <li style={util.styleProtocolListItem}>
      <div
        title={protocol.label}
        style={util.styleBadge(protocol)}
        aria-hidden="true"
      >
        {renderBadgeIcon(protocol)}
      </div>
      <A
        href={url}
        key={url}
        target="_blank"
        style={styleBadgeLink}
        styleFocus={styleBadgeLinkFocused}
      >
        <div
          id={`ListResult${itemId}::Link::${url}`}
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
