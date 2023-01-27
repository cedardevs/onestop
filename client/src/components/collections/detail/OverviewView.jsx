import React from 'react'
import PropTypes from 'prop-types'
import MapThumbnail from '../../common/MapThumbnail'
import FlexRow from '../../common/ui/FlexRow'
import DSMMRating from './DSMMRating'
import Keywords from './Keywords'
import TimeSummary from './TimeSummary'
import SpatialSummary from './SpatialSummary'
import {fontFamilySerif} from '../../../utils/styleUtils'

const styleContainer = {
  padding: '1.618em',
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

const stylePreviewMap = {
  zIndex: 4,
  height: '16em',
  paddingTop: '0.25em',
}

class OverviewView extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      showAllThemes: false,
      showAllInstruments: false,
      showAllPlatforms: false,
    }
  }

  render() {
    const {item} = this.props

    const left = (
      <div key="overview-left" style={styleLeft}>
        <h3 style={styleSectionHeading}>Time Period:</h3>
        <TimeSummary item={item} />

        <div>
          <h3 style={styleSectionHeading}>Map:</h3>
          <div style={stylePreviewMap}>
            <MapThumbnail geometry={item.spatialBounding} interactive={true} />
          </div>
        </div>

        <h3 style={styleSectionHeading}>Bounding Coordinates:</h3>
        <SpatialSummary item={item} />

        <h3 style={styleSectionHeading}>DSMM Rating:</h3>
        <DSMMRating item={item} />
      </div>
    )

    const right = (
      <div key="overview-right" style={styleRight}>
        <Keywords item={item} styleHeading={styleSectionHeading} />
      </div>
    )

    return (
      <div style={styleContainer}>
        <FlexRow
          key="main-section"
          style={{justifyContent: 'space-between'}}
          items={[ left, right ]}
        />
      </div>
    )
  }
}

OverviewView.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default OverviewView
