import React from 'react'
import Expandable from '../common/Expandable'
import Button from '../common/input/Button'
import FlexRow from '../common/FlexRow'
import A from '../common/link/Link'

import trash from 'fa/trash.svg'
import TimeSummary from '../detail/TimeSummary'
import { fontFamilySerif } from '../utils/styleUtils'
import SpatialSummary from '../detail/SpatialSummary'
import MapThumbnail from '../common/MapThumbnail'
import * as util from '../utils/resultUtils'
import { boxShadow } from '../common/defaultStyles'

const styleExpandableWrapper = {
  margin: '0 1.618em 1.618em 0',
  boxShadow: boxShadow,
}

const styleExpandableFocused = {
  // outline: '2px dotted blue'
}

const styleExpandableHeading = {
}

const styleTitle = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.3em',
  color: 'rgb(0, 0, 50)',
  border: '.1em dashed transparent', // prevents resize when focus border is set
  margin: '.259em',
  padding: '.259em',
  overflowWrap: 'break-word',
  wordWrap: 'break-word',
}

const styleExpandableContent = {
  background: '#EEE',
}

const styleSummary = {
  padding: '0.309em'
}

const styleDetail = {
  padding: '1.618em'
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
  fontSize: '1.25em',
  marginTop: '1em',
  marginBottom: '0.25em',
  fontWeight: 'bold',
}

const styleSectionHeadingTop = {
  ...styleSectionHeading,
  marginTop: '0em',
}

const styleDeleteButton = {
  borderRadius: '1em',
  padding: '0 0.309em'
}

const styleDeleteIcon = {
  width: '1.618em',
  height: '1.618em',
}

const stylePreviewMap = {
  zIndex: 4,
  height: '16em',
  paddingTop: '0.25em',
}

export default class CartItem extends React.Component {

  renderBadge = (link, itemId) => {
    const {protocol, url, displayName} = link
    const linkText = displayName ? displayName : protocol.label
    const labelledBy = displayName
        ? // title the link with references to elements: linkText, protocolLegend, granuleTitle
        `ListResult::Link::${url} protocol::legend::${protocol.id}  ListResult::title::${itemId}`
        : // linkText is the same as protocol, so only include one of the two
        `protocol::legend::${protocol.id} ListResult::title::${itemId}`

    return (
        <li key={`accessLink::${url}`} style={util.styleProtocolListItem}>
          <A
              href={url}
              key={url}
              aria-labelledby={labelledBy}
              target="_blank"
              style={{textDecoration: 'none', display: 'inline-flex'}}
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
        </li>
    )
  }

  renderLinks = (links) => {
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
          return this.renderBadge(link, this.props.itemId)
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

    const buttonDelete = (
        <Button
            key={"cartItemDelete"}
            style={styleDeleteButton}
            title="Shopping Cart"
            icon={trash}
            styleIcon={styleDeleteIcon}
            onClick={() => { console.log("click on delete button") }}
        />
    )

    const title = (
        <h2 key={"cartItemTitle"} style={styleTitle}>{item.title}</h2>
    )

    const summaryView = (
        <div style={styleSummary}>
          <FlexRow items={[buttonDelete, title]} />
        </div>
    )

    const mapView = (
        <div aria-hidden={true}>
          <h3 style={styleSectionHeadingTop}>Map:</h3>
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
          <SpatialSummary item={item}/>
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
          <FlexRow items={[left, right]} />
        </div>
    )

    return (
        <Expandable
            styleFocus={styleExpandableFocused}
            styleWrapper={styleExpandableWrapper}
            showArrow={true}
            heading={summaryView}
            styleHeading={styleExpandableHeading}
            content={detailView}
            styleContent={styleExpandableContent}
            value={itemId}
            // open={this.state.citationExpandable}
            // onToggle={handleExpandableToggle}
        />
    )
  }
}