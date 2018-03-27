import React from 'react'
import PropTypes from 'prop-types'
import MapThumbnail from '../common/MapThumbnail'
import {processUrl} from '../../utils/urlUtils'
import {buildCoordinatesString, buildTimePeriodString} from "../../utils/resultUtils";

const styleImageContainer = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}

const styleImage = {
  margin: '0 0 0.618em 0',
  width: '72%',
  maxWidth: '500px',
}

const styleMap = {
  margin: '0 0 0.618em 0',
  width: '100%',
  maxWidth: '500px',
}

const styleSectionHeader = {
  fontSize: '1.25em',
  marginTop: '1em',
  marginBottom: '0.25em',
}

const styleEqualFlexItem = {
  flex: '1 1 auto',
  width: '50%',
}

class ListResult extends React.Component {
  constructor(props) {
    super(props)
  }

  renderDisplayImage(thumbnail, geometry) {
    const imgUrl = processUrl(thumbnail)
    if (imgUrl) {
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
          <MapThumbnail geometry={geometry} interactive={false} />
        </div>
      )
    }
  }

  renderTimeAndSpaceString(beginDate, beginYear, endDate, endYear, spatialBounding) {
    return (
      <div>
        <div>Time Period:</div>
        <div>{buildTimePeriodString(beginDate, beginYear, endDate, endYear)}</div>
        <div>Bounding Coordinates:</div>
        <div>{buildCoordinatesString(spatialBounding)}</div>
      </div>
    )
  }

  renderCitations(citations) {

    return (
      <div>TBD CITATIONS</div>
    )
  }

  renderLinks(links) {

  }

  render() {
    const {item, showLinks, showCitations, showTimeAndSpace} = this.props
    const leftItems = []
    const rightItems = []




    return (
      <div></div>
    )
  }


}


ListResult.propTypes = {
  item: PropTypes.object.isRequired,
  showLinks: PropTypes.bool.isRequired,
  showCitations: PropTypes.bool.isRequired,
  showTimeAndSpace: PropTypes.bool.isRequired
}

export default ListResult