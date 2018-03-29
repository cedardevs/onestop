import React from 'react'
import PropTypes from 'prop-types'
import MapThumbnail from '../../common/MapThumbnail'
import {processUrl} from '../../utils/urlUtils'
import {buildCoordinatesString, buildTimePeriodString, styleBadge, renderBadgeIcon, identifyProtocol} from "../../utils/resultUtils"
import FlexColumn from "../../common/FlexColumn"
import FlexRow from "../../common/FlexRow"
import { boxShadow } from '../../common/defaultStyles'
import A from '../../common/link/Link'

const styleResult = {
  width: '75em',
  height: '20em',
  marginBottom: '2em',
  boxShadow: boxShadow,
  backgroundColor: 'white',
  transition: '0.3s background-color ease'
}

const styleResultFocus = {
  backgroundColor: 'rgb(140, 185, 216)'
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
  fontSize: '1.5em',
  color: 'rgb(0, 0, 50)',
  margin: '0',
  textAlign: 'center'
}

const styleSectionHeader = {
  fontSize: '1.25em',
  margin: '0.25em 0',
}

const styleBadgeLayout = {
  display: 'flex',
  flexFlow: 'row wrap',
}

class ListResult extends React.Component {
  constructor(props) {
    super(props)
  }

  componentWillMount() {
    this.setState({
      focusing: false,
    })
  }

  renderDisplayImage(thumbnail, geometry) {
    const imgUrl = processUrl(thumbnail)
    if (imgUrl && !imgUrl.includes('maps.googleapis.com')) { // Stick to leaflet maps
      return (
        <div style={styleImageContainer}>
          <img
            style={styleImage}
            src={imgUrl}
            alt="Result Image"
            aria-hidden="true"
          />
        </div>
      )
    }
    else {
      // Return map image of spatial bounding or, if none, world map
      return (
        <div style={styleMap}>
          <MapThumbnail geometry={geometry} interactive={true} />
        </div>
      )
    }
  }

  renderTimeAndSpaceString(beginDate, beginYear, endDate, endYear, spatialBounding) {
    return (
      <div>
        <div style={styleSectionHeader}>Time Period:</div>
        <div>{buildTimePeriodString(beginDate, beginYear, endDate, endYear)}</div>
        <div style={styleSectionHeader}>Bounding Coordinates:</div>
        <div>{buildCoordinatesString(spatialBounding)}</div>
      </div>
    )
  }

  renderBadge = ({protocol, url}) => {
    return (
      <A
        href={url}
        key={url}
        title={url}
        target="_blank"
        style={styleBadge(protocol)}
      >
        {renderBadgeIcon(protocol)}
      </A>
    )
  }

  renderLinks(links) {

    const badges = _.chain(links)
      .map(link => ({protocol: identifyProtocol(link), url: link.linkUrl}))
      .filter(info => info.protocol)
      .sortBy(info => info.protocol.id)
      .map(this.renderBadge.bind(this))
      .value()

    const badgesElement = _.isEmpty(badges) ? <div>N/A</div> : <div style={styleBadgeLayout}>{badges}</div>

    return (
      <div>
        <div style={styleSectionHeader}>Data Access Links:</div>
        <div style={{paddingBottom: '1em'}}>{badgesElement}</div>
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

  render() {
    const {item, showLinks, showTimeAndSpace} = this.props
    const rightItems = [<h2 style={styleTitle}>{item.title}</h2>]

    if (showLinks) {
      rightItems.push(this.renderLinks(item.links))
    }
    if (showTimeAndSpace) {
      rightItems.push(this.renderTimeAndSpaceString(item.beginDate, item.beginYear, item.endDate, item.endYear, item.spatialBounding))
    }

    const left = <FlexColumn style={{width: '32%'}} items={[this.renderDisplayImage(item.thumbnail, item.spatialBounding)]} />
    const right = <FlexColumn style={{marginLeft: '2em', width: '68%'}} items={rightItems}/>

    const styleResultMerged = {
      ...styleResult,
      ...(this.state.focusing ? styleResultFocus : {})
    }

    return (
      <div style={styleResultMerged} onFocus={this.handleFocus} onBlur={this.handleBlur}>
        <FlexRow style={{padding: '2em'}} items={[left, right]}/>
      </div>
    )
  }


}


ListResult.propTypes = {
  item: PropTypes.object.isRequired,
  showLinks: PropTypes.bool.isRequired,
  showTimeAndSpace: PropTypes.bool.isRequired
}

export default ListResult