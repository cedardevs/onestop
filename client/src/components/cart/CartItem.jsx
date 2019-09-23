import React from 'react'
import Expandable from '../common/ui/Expandable'
import FlexRow from '../common/ui/FlexRow'
import A from '../common/link/Link'

import TimeSummary from '../collections/detail/TimeSummary'
import {fontFamilySerif} from '../../utils/styleUtils'
import SpatialSummary from '../collections/detail/SpatialSummary'
import MapThumbnail from '../common/MapThumbnail'
import * as util from '../../utils/resultUtils'
import {boxShadow} from '../../style/defaultStyles'
import ActionPane from './ActionPane'
import FlexColumn from '../common/ui/FlexColumn'
import {processUrl} from '../../utils/urlUtils'
const pattern = require('../../../img/topography.png')

const styleWrapper = {
  margin: '0 1.618em 0.618em 0',
  boxShadow: boxShadow,
  borderRadius: '0 0.309em 0.309em 0',
}

const styleExpandableWrapper = {
  width: '100%',
}

const styleExpandableHeadingFocused = {
  textDecoration: 'underline',
  outline: '2px dashed black',
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

const styleExpandableHeading = open => {
  return {
    borderBottom: open ? '1px solid #EEE' : 'initial',
    justifyContent: 'space-between',
    padding: '0.618em',
    color: 'black',
  }
}

const styleTitle = open => {
  return {
    fontFamily: fontFamilySerif(),
    fontSize: '1em',
    fontWeight: !open ? 'normal' : 'bold',
    overflowWrap: 'break-word',
    wordWrap: 'break-word',
    margin: 0,
  }
}

const styleSummary = {
  padding: '0',
}

const styleDetail = {
  padding: '0.618em',
}

const styleLeftRightFlexRow = {
  flexDirection: 'row-reverse',
}

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

const styleSectionHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  marginTop: '1em',
  marginBottom: '0.25em',
  fontWeight: 'bold',
}

const styleSectionHeadingTop = {
  ...styleSectionHeading,
  marginTop: '0em',
}

const styleMap = {
  zIndex: 4,
  width: '100%',
  height: '16em',
  paddingTop: '0.25em',
}

const styleBadgeLink = {
  textDecoration: 'none',
  display: 'inline-flex',
  paddingRight: '0.309em',
}

const styleBadgeLinkFocused = {
  outline: '2px dashed black',
  outlineOffset: '0.105em',
}

const styleLinkText = {
  textDecoration: 'underline',
  margin: '0.6em 0',
}

export default class CartItem extends React.Component {
  constructor(props) {
    super(props)
    this.state = {expanded: false}
    this.props = props
  }

  handleExpandableToggle = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        expanded: !!event.open,
      }
    })
  }

  renderDisplayImage(thumbnail, geometry) {
    const imgUrl = processUrl(thumbnail)
    if (imgUrl && !imgUrl.includes('maps.googleapis.com')) {
      // Stick to leaflet maps
      return (
        <div key={'CartItem::image'} style={styleImageContainer}>
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
        <div key={'CartItem::map'} aria-hidden={true}>
          <div style={styleMap}>
            <MapThumbnail geometry={geometry} interactive={true} />
          </div>
        </div>
      )
    }
  }

  renderBadge = (link, itemId, item) => {
    const {protocol, url, displayName} = link
    const linkText = displayName ? displayName : protocol.label

    const ariaTitle = displayName
      ? `${linkText} ${protocol.label} ${item.title}`
      : `${linkText} ${item.title}`

    return (
      <li key={`accessLink::${url}`} style={util.styleProtocolListItem}>
        <A
          href={url}
          key={url}
          title={ariaTitle}
          target="_blank"
          style={styleBadgeLink}
          styleFocus={styleBadgeLinkFocused}
        >
          <div style={util.styleBadge(protocol)} aria-hidden="true">
            {util.renderBadgeIcon(protocol)}
          </div>
          <div style={styleLinkText}>{linkText}</div>
        </A>
      </li>
    )
  }

  renderLinks = links => {
    const badges = _.chain(links)
      // .filter(link => link.linkFunction.toLowerCase() === 'download' || link.linkFunction.toLowerCase() === 'fileaccess')
      .map(link => ({
        protocol: util.identifyProtocol(link),
        url: link.linkUrl,
        displayName: link.linkName
          ? link.linkName
          : link.linkDescription ? link.linkDescription : null,
      }))
      .sortBy(info => info.protocol.id)
      .map(link => {
        return this.renderBadge(link, this.props.itemId, this.props.item)
      })
      .value()
    const badgesElement = _.isEmpty(badges) ? 'N/A' : badges

    return (
      <div>
        <h3 style={styleSectionHeadingTop}>Data Access Links:</h3>
        <ul style={util.styleProtocolList}>{badgesElement}</ul>
      </div>
    )
  }

  render() {
    const {itemId, item, handleExpandableToggle} = this.props

    const title = (
      <h3 key={'cartItemTitle'} style={styleTitle(this.state.expanded)}>
        {item.title}
      </h3>
    )

    const summaryView = (
      <div style={styleSummary}>
        <FlexRow items={[ title ]} />
      </div>
    )

    const dataAccessLinks = this.renderLinks(item.links)

    const timePeriod = (
      <div>
        <h3 style={styleSectionHeading}>Time Period:</h3>
        <TimeSummary item={item} />
      </div>
    )

    const boundingCoordinates = (
      <div>
        <h3 style={styleSectionHeading}>Bounding Coordinates:</h3>
        <SpatialSummary item={item} />
      </div>
    )

    const left = (
      <FlexColumn
        key={'overview-left'}
        style={styleLeft}
        items={[
          this.renderDisplayImage(item.thumbnail, item.spatialBounding),
        ]}
      />
    )

    const right = (
      <FlexColumn
        key={'overview-right'}
        style={styleRight}
        items={[ dataAccessLinks, timePeriod, boundingCoordinates ]}
      />
    )

    const detailView = (
      <div style={styleDetail}>
        <FlexRow items={[ right, left ]} style={styleLeftRightFlexRow} />
      </div>
    )

    const expandable = (
      <Expandable
        key={'cartItemExpandable'}
        styleHeadingFocus={styleExpandableHeadingFocused}
        styleWrapper={styleExpandableWrapper}
        showArrow={true}
        arrowTextClosed={'show details'}
        arrowTextOpened={'hide details'}
        styleArrowText={{fontSize: '0.8em'}}
        alignArrow={true}
        heading={summaryView}
        headingTitle={item.title}
        styleHeading={styleExpandableHeading(this.state.expanded)}
        content={detailView}
        value={itemId}
        onToggle={this.handleExpandableToggle}
        open={this.state.expanded}
      />
    )

    const {deselectGranule} = this.props
    const actionPane = (
      <ActionPane
        key={'cartItemActionPane'}
        expanded={this.state.expanded}
        item={item}
        itemId={itemId}
        deselectGranule={deselectGranule}
      />
    )

    return (
      <div style={styleWrapper}>
        <FlexRow items={[ expandable, actionPane ]} />
      </div>
    )
  }
}
