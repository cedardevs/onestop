import React, {Component} from 'react'
import PropTypes from 'prop-types'
import MapThumbnail from '../common/MapThumbnail'
import FlexColumn from '../common/FlexColumn'
import FlexRow from '../common/FlexRow'
import DSMMRating from './DSMMRating'
import Keywords from './Keywords'
import TimeSummary from './TimeSummary'
import SpatialSummary from './SpatialSummary'
import GranulesSummary from './GranulesSummary'

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
  fontSize: '1.25em',
  marginTop: '1em',
  marginBottom: '0.25em',
  fontWeight: 'normal',
}

const stylePreviewMap = {
  zIndex: 4,
  height: '16em',
  paddingTop: '0.25em',
}

class OverviewView extends Component {
  constructor(props) {
    super(props)
    this.state = {
      showAllThemes: false,
      showAllInstruments: false,
      showAllPlatforms: false,
    }
  }

  render() {
    const {item, totalGranuleCount, navigateToGranules} = this.props

    const left = (
      <div key="overview-left" style={styleLeft}>
        <h3 style={styleSectionHeading}>Time Period:</h3>
        <TimeSummary item={item} />

        <div aria-hidden={true}>
          <h3 style={styleSectionHeading}>Spatial Bounding Map:</h3>
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

    const granuleSection = (
      <GranulesSummary
        key="granule-summary-section"
        totalGranuleCount={totalGranuleCount}
        navigateToGranules={navigateToGranules}
      />
    )

    const mainSection = (
      <FlexRow
        key="main-section"
        style={{justifyContent: 'space-between'}}
        items={[ left, right ]}
      />
    )

    return (
      <div style={styleContainer}>
        <FlexColumn items={[ granuleSection, mainSection ]} />
      </div>
    )
  }
}

OverviewView.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default OverviewView
