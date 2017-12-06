import React, { Component } from 'react'
import FlexColumn from '../common/FlexColumn'
import FlexRow from '../common/FlexRow'
import Button from '../common/input/Button'

import MapFitlerBoundingBoxInput from './MapFilterBoundingBoxInput'

import mapIcon from '../../img/font-awesome/white/svg/globe.svg'


const styleMapFilter = {
  backgroundColor: '#5396CC',
  padding: '0.618em',
  color: '#F9F9F9',
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

export default class MapFilter extends Component {
  render() {
    const inputBoundingBox = <MapFitlerBoundingBoxInput key="MapFilter::inputBoundingBox"  />

    const buttonShowMap = (
      <Button key="MapFilter::showMap" icon={mapIcon} text="Show Map" onClick={() => console.log('show map')} style={{marginBottom: '0.309em'}} />
    )

    const buttonApply = (
      <Button
          key="MapFilter::apply"
          text="Apply"
        onClick={() => console.log('apply map')}
        style={{ marginRight: '0.309em', width: '50%' }}
      />
    )

    const buttonClear = (
      <Button
        key="MapFilter::clear"
        text="Clear"
        onClick={() => console.log('clear map')}
        style={{ width: '50%' }}
      />
    )

    const rowButtons = (
      <FlexRow key="MapFilter::rowButtons" items={[buttonApply, buttonClear]} style={styleApplyClear} />
    )

    const inputColumn = (
      <FlexColumn items={[inputBoundingBox, buttonShowMap, rowButtons]} />
    )

    return (
      <div style={styleMapFilter}>
        <p style={styleDescription}>
          Filter your search results by manually entering coordinates or
          selecting a boundary on the map:
        </p>
        <h4>Bounding Box:</h4>
        {inputColumn}
      </div>
    )
  }
}
