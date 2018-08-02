import React from 'react'
import ReactDOM from 'react-dom'
import PropTypes from 'prop-types'
import MapThumbnail from '../../common/MapThumbnail'
import {processUrl} from '../../utils/urlUtils'
import * as util from '../../utils/resultUtils'
import FlexColumn from '../../common/FlexColumn'
import FlexRow from '../../common/FlexRow'
import {boxShadow} from '../../common/defaultStyles'
import A from '../../common/link/Link'
import {fontFamilySerif} from '../../utils/styleUtils'
import Checkbox from '../../common/input/Checkbox'

const styleResult = {
  minHeight: '15.5em',
  margin: '0 1.618em 1.618em 0',
  boxShadow: boxShadow,
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
  fontSize: '1.3em',
  color: 'rgb(0, 0, 50)',
  border: '.1em dashed transparent', // prevents resize when focus border is set
  margin: '.259em',
  padding: '.259em',
  overflowWrap: 'break-word',
  wordWrap: 'break-word',
}

const styleSectionHeader = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.1em',
  margin: '0.618em 0 0 0',
}

const styleSectionContent = {
  margin: '0.309em 0 0 0',
}

const styleFocusDefault = {
  outline: 'none',
  border: '.1em dashed white',
}

class ListResult extends React.Component {

  componentWillMount() {
    this.setState({
      focusing: false,
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

  renderLinks(links) {
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
      <div key={'ListResult::accessLinks'}>
        <h3 style={styleSectionHeader}>Data Access Links:</h3>
        <ul style={util.styleProtocolList}>{badgesElement}</ul>
      </div>
    )
  }

  handleFocus = event => {
    this.setState({
      focusing: true,
    })
  }

  handleBlur = event => {
    this.setState({
      focusing: false,
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
    const {itemId, item, showLinks, showTimeAndSpace, handleCheckboxChange, checkGranule} = this.props

    const styleFocused = {
      ...(this.state.focusing ? styleFocusDefault : {}),
    }

    const styleOverallHeadingApplied = {
      ...styleTitle,
      ...styleFocused,
    }

    const rightItems = [
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
      </h2>,
    ]

    const selectGranuleCheckbox = (
        <Checkbox
            key={`checkbox-${itemId}`}
            label={`Add to Cart: ${itemId}`}
            id={itemId}
            checked={checkGranule}
            onChange={handleCheckboxChange(itemId, item)}
        />
    )

    if (showLinks) {
      rightItems.push(this.renderLinks(item.links))
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
        style={{width: '32%'}}
        items={[
          this.renderDisplayImage(item.thumbnail, item.spatialBounding),
        ]}
      />
    )
    const right = (
      <FlexColumn
        key={'ListResult::rightColumn'}
        style={{marginLeft: '1.618em', width: '68%'}}
        items={rightItems}
      />
    )

    const styleResultMerged = {
      ...styleResult,
      ...(this.state.focusing ? styleResultFocus : {}),
    }

    return (
      <div
        style={styleResultMerged}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
      >
        <FlexRow
          style={{padding: '1.618em', flexDirection: 'row-reverse'}}
          items={[ selectGranuleCheckbox, right, left ]}
        />
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
