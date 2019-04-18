import React from 'react'
import Expandable from '../common/Expandable'
import FlexRow from '../common/FlexRow'
import A from '../common/link/Link'

import TimeSummary from '../detail/TimeSummary'
import {fontFamilySerif} from '../utils/styleUtils'
import SpatialSummary from '../detail/SpatialSummary'
import MapThumbnail from '../common/MapThumbnail'
import * as util from '../utils/resultUtils'
import {boxShadow} from '../common/defaultStyles'
import ActionPane from './ActionPane'

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
  width: '50%',
}

const styleRight = {
  flex: '1 1 auto',
  width: '50%',
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

const stylePreviewMap = {
  zIndex: 4,
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
      <h2 key={'cartItemTitle'} style={styleTitle(this.state.expanded)}>
        {item.title}
      </h2>
    )

    const summaryView = (
      <div style={styleSummary}>
        <FlexRow items={[ title ]} />
      </div>
    )

    const mapView = (
      <div aria-hidden={true}>
        <div style={stylePreviewMap}>
          <MapThumbnail geometry={item.spatialBounding} interactive={true} />
        </div>
      </div>
    )

    const left = (
      <div key="overview-left" style={styleLeft}>
        {mapView}
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

    const right = (
      <div key="overview-right" style={styleRight}>
        {dataAccessLinks}
        {timePeriod}
        {boundingCoordinates}
      </div>
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
