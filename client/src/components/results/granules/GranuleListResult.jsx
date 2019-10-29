import React from 'react'
import ReactDOM from 'react-dom'
import PropTypes from 'prop-types'
import GranuleAccessLink from './GranuleAccessLink'
import MapThumbnail from '../../common/MapThumbnail'
import {processUrl} from '../../../utils/urlUtils'
import * as util from '../../../utils/resultUtils'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'
import {boxShadow} from '../../../style/defaultStyles'
import {fontFamilySerif} from '../../../utils/styleUtils'
import Checkbox from '../../common/input/Checkbox'
import {FEATURE_CART} from '../../../utils/featureUtils'
import VideoTray from './VideoTray'
import {granuleDownloadableLinks} from '../../../utils/cartUtils'
const pattern = require('../../../../img/topography.png')

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

const styleLeft = {
  width: '38.2%',
  background: `url(${pattern}) repeat`,
  backgroundSize: '30em',
  justifyContent: 'center',
}

const styleRight = {
  marginLeft: '1.618em',
  width: '61.8%',
}

const styleImageContainer = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  height: '100%',
}

const styleImage = {
  width: '100%',
  height: '16em',
  objectFit: 'contain',
}

const styleMap = {
  width: '100%',
  height: '16em',
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

const styleFlexRowR2L = {
  flexDirection: 'row-reverse',
  flexGrow: 1,
}

class ListResult extends React.Component {
  UNSAFE_componentWillMount() {
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
        <h4 style={styleSectionHeader}>Time Period:</h4>
        <div style={styleSectionContent}>
          {util.buildTimePeriodString(beginDate, beginYear, endDate, endYear)}
        </div>
        <h4 style={styleSectionHeader}>Bounding Coordinates:</h4>
        <div style={styleSectionContent}>
          {util.buildCoordinatesString(spatialBounding)}
        </div>
      </div>
    )
  }

  renderServiceLinks = serviceLinks => {
    const services =
      serviceLinks && serviceLinks.length > 0 ? (
        serviceLinks.map(service => {
          return this.renderLinks(service.links)
        })
      ) : (
        <div style={styleSectionContent}>None available</div>
      )

    return (
      <div key={'ListResult::serviceLinks'}>
        <h4 style={styleSectionHeader}>Service Links:</h4>
        {services}
      </div>
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
      .map((link, index) => {
        return (
          <GranuleAccessLink
            key={`accessLink::${this.props.itemId}::${index}`}
            link={link}
            item={this.props.item}
            itemId={this.props.itemId}
            showGranuleVideo={(linkProtocol, url, focusRef, itemId) => {
              this.props.showGranuleVideo(itemId)
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
          />
        )
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
      <h3
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
      </h3>
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
          <h4 style={styleSectionHeader}>Data Access Links:</h4>
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
        style={styleLeft}
        items={[
          this.renderDisplayImage(item.thumbnail, item.spatialBounding),
        ]}
      />
    )

    const right = (
      <FlexColumn
        key={'ListResult::rightColumn'}
        style={styleRight}
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
