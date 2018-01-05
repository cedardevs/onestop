import React from 'react'
import FlexColumn from '../common/FlexColumn'
import FlexRow from '../common/FlexRow'
import Button from '../common/input/Button'

import MapFitlerBoundingBoxInput from './MapFilterBoundingBoxInput'
import ArcGISMap from '../search/map/ArcGISMap'

import mapIcon from '../../img/font-awesome/white/svg/globe.svg'

const styleMapFilter = {
  backgroundColor: '#5396CC',
  padding: '0.618em',
  color: '#F9F9F9',
  position: 'relative',
}

const styleDescription = {
  margin: 0,
}

const styleInputColumn = {
  alignItems: 'center',
}

const styleApplyClear = {
  justifyContent: 'center',
  alignSelf: 'stretch',
}

export default class MapFilter extends React.Component {

  handleShowMap = () => {
    const { showMap, toggleMap } = this.props
    if(!showMap && toggleMap) {
      toggleMap()
    }
  }

  handleHideMap = () => {
    const { showMap, toggleMap } = this.props
    if(showMap && toggleMap) {
      toggleMap()
    }
  }

  render() {

    const { toggleExcludeGlobal, showMap, bounds, boundsSource, updateBounds, geoJSON } = this.props

    // TODO: implement with ArcGIS/React interface for accessibility
    const inputBoundingBox = null
    // const inputBoundingBox = (
    //   <MapFitlerBoundingBoxInput key="MapFilter::inputBoundingBox" bounds={bounds} boundsSource={boundsSource} updateBounds={updateBounds} />
    // )

    const styleShowOrApplyBackground = geoJSON ? { background: '#8967d2' } : {}
    const styleShowOrApply = { marginBottom: '0.309em', ...styleShowOrApplyBackground }

    const buttonShowMap = (
      <Button
        key="MapFilter::showMap"
        icon={mapIcon}
        text="Show Map"
        onClick={this.handleShowMap}
        style={styleShowOrApply}
        aria-hidden={true}
      />
    )

    const buttonApply = (
      <Button
        key="MapFilter::apply"
        icon={mapIcon}
        text="Apply"
        onClick={this.handleHideMap}
        style={styleShowOrApply}
      />
    )

    const inputColumn = (
      <FlexColumn items={[inputBoundingBox, showMap ? buttonApply : buttonShowMap]} />
    )

    return (
      <div style={styleMapFilter}>
        <p style={styleDescription}>
          Filter your search results by selecting a boundary on the map:
        </p>
        <div style={{height:'1em'}} />
        {/*<p style={styleDescription}>*/}
          {/*Filter your search results by manually entering coordinates or*/}
          {/*selecting a boundary on the map:*/}
        {/*</p>*/}
        {/*<h4>Bounding Box:</h4>*/}
        {inputColumn}
      </div>
    )
  }
}
