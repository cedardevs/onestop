import React from 'react'
import ReactDOM from 'react-dom'
import PropTypes from 'prop-types'
import MapThumbnail from '../../common/MapThumbnail'
import {processUrl, isGovExternal} from '../../../utils/urlUtils'
import * as util from '../../../utils/resultUtils'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'
import {boxShadow} from '../../../style/defaultStyles'
import A from '../../common/link/Link'
import Button from '../../common/input/Button'
import {fontFamilySerif} from '../../../utils/styleUtils'
import Checkbox from '../../common/input/Checkbox'
import {FEATURE_CART} from '../../../utils/featureUtils'
import {play_circle_o, SvgIcon} from '../../common/SvgIcon'
import VideoTray from './VideoTray'
import {granuleDownloadableLinks} from '../../../utils/cartUtils'

const styleResult = {
  minHeight: '15.5em',
  margin: '0 1.618em 1em 0',
  padding: '0.618em',
  boxShadow: boxShadow,
  borderRadius: '0.309em',
  backgroundColor: 'white',
  transition: '0.3s background-color ease',
}

const styleResultFocus = {
  backgroundColor: 'rgb(140, 185, 216)',
}

const styleImageContainer = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}

const styleImage = {
  width: '100%',
  height: '15.5em',
}

const styleMap = {
  width: '100%',
  height: '15.5em',
}

const styleTitle = {
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  color: 'rgb(0, 0, 50)',
  overflowWrap: 'break-word',
  wordWrap: 'break-word',
  margin: '0 0 0.618em 0',
}

const styleSectionHeader = {
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  margin: '0.618em 0 0 0',
}

const styleSectionContent = {
  margin: '0.309em 0 0 0',
}

const styleFocusDefault = {
  outline: 'none',
  // border: '.1em dashed white',
  textDecoration: 'underline',
}

const styleBadgeLink = {
  textDecoration: 'none',
  display: 'inline-flex',
  paddingRight: '0.309em',
}

const styleBadgeLinkFocused = {
  outline: '2px dashed white',
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

const styleFlexRowR2L = {
  flexDirection: 'row-reverse',
  flexGrow: 1,
}

class ListResult extends React.Component {
  componentWillMount() {
    this.setState({
      focusing: false,
      videoPlaying: null,
    })
  }

  componentDidMount() {
    if (this.props.shouldFocus) {
      ReactDOM.findDOMNode(this.focusItem).focus()
    }
  }

  renderDisplayImage(thumbnail, geometry) {
    const imgUrl = processUrl(thumbnail)
    if (imgUrl && !imgUrl.includes('maps.googleapis.com')) {
      // Stick to leaflet maps
      return (
        <div key={'ListResult::image'} style={styleImageContainer}>
          <img
            style={styleImage}
            src={imgUrl}
            alt=""
            aria-hidden="true"
            width="100px"
            height="100px"
          />
        </div>
      )
    }
    else {
      // Return map image of spatial bounding or, if none, world map
      return (
        <div key={'ListResult::map'} style={styleMap}>
          <MapThumbnail geometry={geometry} interactive={true} />
        </div>
      )
    }
  }

  renderTimeAndSpaceString(
    beginDate,
    beginYear,
    endDate,
    endYear,
    spatialBounding
  ) {
    return (
      <div key={'ListResult::timeAndSpace'}>
        <h3 style={styleSectionHeader}>Time Period:</h3>
        <div style={styleSectionContent}>
          {util.buildTimePeriodString(beginDate, beginYear, endDate, endYear)}
        </div>
        <h3 style={styleSectionHeader}>Bounding Coordinates:</h3>
        <div style={styleSectionContent}>
          {util.buildCoordinatesString(spatialBounding)}
        </div>
      </div>
    )
  }

  renderServiceLinks = serviceLinks => {
    const services = serviceLinks ? (
      serviceLinks.map(service => {
        return this.renderLinks(service.links)
      })
    ) : (
      <div style={styleSectionContent}>None available</div>
    )

    return (
      <div key={'ListResult::serviceLinks'}>
        <h3 style={styleSectionHeader}>Service Links:</h3>
        {services}
      </div>
    )
  }

  renderBadge = (link, itemId) => {
    const {protocol, url, displayName, linkProtocol} = link
    const linkText = displayName ? displayName : protocol.label
    const labelledBy = displayName
      ? // title the link with references to elements: linkText, protocolLegend, granuleTitle
        `ListResult::Link::${url} protocol::legend::${protocol.id}  ListResult::title::${itemId}`
      : // linkText is the same as protocol, so only include one of the two
        `protocol::legend::${protocol.id} ListResult::title::${itemId}`
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
    return (
      <li key={`accessLink::${url}`} style={util.styleProtocolListItem}>
        <A
          href={url}
          key={url}
          aria-labelledby={labelledBy}
          target="_blank"
          style={styleBadgeLink}
          styleFocus={styleBadgeLinkFocused}
        >
          <div style={util.styleBadge(protocol)} aria-hidden="true">
            {util.renderBadgeIcon(protocol)}
          </div>
          <div
            id={`ListResult::Link::${url}`}
            style={{
              ...{
                textDecoration: 'underline',
                margin: '0.6em 0',
              },
            }}
          >
            {linkText}
          </div>
        </A>
        {videoPlay}
      </li>
    )
  }

  renderLinks(links) {
    const badges = _.chain(links)
      // .filter(link => link.linkFunction.toLowerCase() === 'download' || link.linkFunction.toLowerCase() === 'fileaccess')
      .map(link => ({
        protocol: util.identifyProtocol(link),
        url: link.linkUrl,
        displayName: link.linkName
          ? link.linkName
          : link.linkDescription ? link.linkDescription : null,
        linkProtocol: link.linkProtocol, // needed to handle videos consistently
      }))
      .sortBy(info => info.protocol.id)
      .map(link => {
        return this.renderBadge(link, this.props.itemId)
      })
      .value()
    const badgesElement = _.isEmpty(badges) ? 'N/A' : badges

    return badgesElement
  }

  handleFocus = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  handleFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {
      itemId,
      item,
      granuleVideoId,
      showLinks,
      showTimeAndSpace,
      handleCheckboxChange,
      checkGranule,
      featuresEnabled,
    } = this.props

    const {videoPlaying} = this.state

    const styleFocused = {
      ...(this.state.focusing ? styleFocusDefault : {}),
    }

    const styleOverallHeadingApplied = {
      ...styleTitle,
      ...styleFocused,
    }

    const title = (
      <h2
        id={`ListResult::title::${itemId}`}
        key={`ListResult::title::${itemId}`}
        tabIndex={-1}
        ref={header => {
          this.focusItem = header
        }}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        style={styleOverallHeadingApplied}
      >
        {item.title}
      </h2>
    )

    const granuleDownloadable = granuleDownloadableLinks([ item ]).length > 0
    const selectGranuleCheckbox =
      featuresEnabled.includes(FEATURE_CART) && granuleDownloadable ? (
        <Checkbox
          key={`checkbox-${itemId}`}
          title={`Add ${item.title} to cart`}
          label={`Add to cart`}
          id={itemId}
          checked={checkGranule}
          onChange={handleCheckboxChange(itemId, item)}
        />
      ) : null

    const rightItems = []
    if (showLinks) {
      rightItems.push(
        <div key={'ListResult::accessLinks'}>
          <h3 style={styleSectionHeader}>Data Access Links:</h3>
          <ul style={util.styleProtocolList}>{this.renderLinks(item.links)}</ul>
        </div>
      )
    }
    if (showLinks) {
      rightItems.push(this.renderServiceLinks(item.serviceLinks))
    }
    if (showTimeAndSpace) {
      rightItems.push(
        this.renderTimeAndSpaceString(
          item.beginDate,
          item.beginYear,
          item.endDate,
          item.endYear,
          item.spatialBounding
        )
      )
    }
    const left = (
      <FlexColumn
        key={'ListResult::leftColumn'}
        style={{width: '50%'}}
        items={[
          this.renderDisplayImage(item.thumbnail, item.spatialBounding),
        ]}
      />
    )

    const right = (
      <FlexColumn
        key={'ListResult::rightColumn'}
        style={{marginLeft: '1.618em', width: '50%'}}
        items={rightItems}
      />
    )

    const styleResultMerged = {
      ...styleResult,
      ...(this.state.focusing ? styleResultFocus : {}),
    }

    const video = (
      <VideoTray
        closeTray={() => {
          this.props.showGranuleVideo(null)
        }}
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
    )

    const flexRowR2L = (
      <FlexRow style={styleFlexRowR2L} items={[ right, left ]} />
    )

    return (
      <div
        style={styleResultMerged}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
      >
        {title}
        {flexRowR2L}
        {video}
        <div style={{marginTop: '1em'}}>{selectGranuleCheckbox}</div>
      </div>
    )
  }
}

ListResult.propTypes = {
  itemId: PropTypes.string.isRequired,
  item: PropTypes.object.isRequired,
  showLinks: PropTypes.bool.isRequired,
  showTimeAndSpace: PropTypes.bool.isRequired,
}

export default ListResult
