import React, {useState} from 'react'
import FlexRow from '../../common/ui/FlexRow'
import {fontFamilySerif} from '../../../utils/styleUtils'
import FlexColumn from '../../common/ui/FlexColumn'
import TimeSummary from '../../collections/detail/TimeSummary'
import SpatialSummary from '../../collections/detail/SpatialSummary'
import ResultGraphic from '../../results/ResultGraphic'
import ResultAccessLinks from '../../results/ResultAccessLinks'
import VideoTray from './VideoTray'
import ReactDOM from 'react-dom'
import {granuleDownloadableLinks} from '../../../utils/cartUtils'
import {FEATURE_CART} from '../../../utils/featureUtils'
import {getApiRegistryPath} from '../../../utils/urlUtils'
import Checkbox from '../../common/input/Checkbox'

const pattern = require('../../../../img/topography.png')

const styleLeft = {
  flex: '1 1 auto',
  width: '38.2%',
  background: `url(${pattern}) repeat`,
  backgroundSize: '30em',
  justifyContent: 'center',
}

const styleRight = {
  flex: '1 1 auto',
  width: '61.8%',
  marginLeft: '1.618em',
}

const styleLeftRightFlexRow = {
  flexDirection: 'row-reverse',
}

const styleContent = {
  padding: '0.618em',
}

const styleContentHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  marginTop: '1em',
  marginBottom: '0.25em',
  fontWeight: 'bold',
}

const styleContentHeadingTop = {
  ...styleContentHeading,
  marginTop: '0em',
}

export default function GranuleItem(props){
  const [ videoPlaying, setVideoPlaying ] = useState(null)

  const {
    itemId,
    item,
    showAccessLinks,
    showVideos,
    featuresEnabled,
    granuleVideoId,
    showGranuleVideo,
  } = props

  const handleShowGranuleVideo = showVideos
    ? (linkProtocol, url, focusRef, itemId) => {
        if (showGranuleVideo) {
          showGranuleVideo(itemId)
        }
        setVideoPlaying({
          protocol: linkProtocol,
          url: url,
          returnFocusRef: focusRef,
        })
      }
    : null

  const accessLinks = showAccessLinks ? (
    <div key={'GranuleListItem::accessLinks'}>
      <h4 style={styleContentHeadingTop}>Data Access Links:</h4>
      <ResultAccessLinks
        itemId={itemId}
        item={item}
        handleShowGranuleVideo={handleShowGranuleVideo}
      />
    </div>
  ) : null

  const xmlLink = (
    <a
      href={getApiRegistryPath() + '/metadata/granule/' + itemId + '/raw'}
      target={'_blank'}
    >
      Download raw metadata
    </a>
  )

  const metadataLinks = (
    <div key={'GranuleListItem::metadataLinks'}>
      <h4 style={styleContentHeadingTop}>Metadata Access:</h4>
      {xmlLink}
    </div>
  )

  const timePeriod = (
    <div key={'GranuleListItem::timePeriod'}>
      <h4 style={styleContentHeading}>Time Period:</h4>
      <TimeSummary item={item} />
    </div>
  )

  const boundingCoordinates = (
    <div key={'GranuleListItem::boundingCoordinates'}>
      <h4 style={styleContentHeading}>Bounding Coordinates:</h4>
      <SpatialSummary item={item} />
    </div>
  )

  const left = (
    <FlexColumn
      key={'GranuleListItem::left'}
      style={styleLeft}
      items={[
        <ResultGraphic
          key={'ResultGraphic'}
          thumbnail={item.thumbnail}
          geometry={item.spatialBounding}
          height={'16em'}
        />,
      ]}
    />
  )

  const right = (
    <FlexColumn
      key={'GranuleListItem::right'}
      style={styleRight}
      items={[ accessLinks, metadataLinks, timePeriod, boundingCoordinates ]}
    />
  )

  const video = showVideos ? (
    <VideoTray
      /* TODO: Figure out the culprit to this error when 'x'ing out of a video:
       *   backend.js:6 Warning: unstable_flushDiscreteUpdates: Cannot flush updates when React is already rendering.
       */
      closeTray={() => showGranuleVideo(null)}
      trayCloseComplete={() => {
        ReactDOM.findDOMNode(videoPlaying.returnFocusRef).scrollIntoView({
          behavior: 'smooth',
        })
        ReactDOM.findDOMNode(videoPlaying.returnFocusRef).focus()
      }}
      url={videoPlaying ? videoPlaying.url : null}
      protocol={videoPlaying ? videoPlaying.protocol : null}
      showVideo={videoPlaying && granuleVideoId === itemId}
    />
  ) : null

  const granuleDownloadable = granuleDownloadableLinks([ item ]).length > 0
  const selectGranuleCheckbox =
    featuresEnabled.includes(FEATURE_CART) && granuleDownloadable ? (
      <Checkbox
        key={`checkbox-${itemId}`}
        title={`Add ${item.title} to cart`}
        label={`Add to cart`}
        id={itemId}
        checked={props.checkGranule}
        onChange={props.handleCheckboxChange(itemId, item)}
      />
    ) : null

  return (
    <div style={styleContent}>
      <FlexRow items={[ right, left ]} style={styleLeftRightFlexRow} />
      {video}
      <div style={{marginTop: '1em'}}>{selectGranuleCheckbox}</div>
    </div>
  )
}
