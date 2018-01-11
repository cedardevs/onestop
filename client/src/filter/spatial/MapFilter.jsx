import React, { Component } from 'react'
import FlexColumn from '../../common/FlexColumn'
import Button from '../../common/input/Button'

import mapIcon from '../../../img/font-awesome/white/svg/globe.svg'
import Checkbox from "../../common/input/Checkbox"
import MapFilterCoordinatesInput from "./MapFilterCoordinatesInput"
import {convertBboxToGeoJson} from "../../utils/geoUtils";

const styleMapFilter = {
  backgroundColor: '#3D97D2',
  padding: '0.618em',
  color: '#F9F9F9',
  position: 'relative',
}

const styleDescription = {
  margin: 0,
}

const styleButtons = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-around',
  marginTop: '1em'
}

export default class MapFilter extends Component {

  constructor(props) {
    super(props)

    this.state = this.initialState()
  }

  initialState() {
    return {
      bboxWest: null,
      bboxSouth: null,
      bboxEast: null,
      bboxNorth: null,
      internalGeoJSON: null,
    }
  }

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

  toggleExcludeGlobalResults = () => {
    this.props.toggleExcludeGlobal()
    this.props.submit()
  }

  updateBboxCoords = (west, south, east, north) => {
    console.log('update coords called, state was: ',this.state)
    this.setState({
      bboxWest: west,
      bboxSouth: south,
      bboxEast: east,
      bboxNorth: north,
      internalGeoJSON: convertBboxToGeoJson(west, south, east, north)
    })
  }

  render() {

    const inputBoundingBox = (
      <MapFilterCoordinatesInput key='MapFilter::coordsInput' updateUpstreamCoords={this.updateBboxCoords} geoJSON={this.props.geoJSON}/>
    )

    const styleShowOrHideBackground = this.props.geoJSON ? { background: '#8967d2' } : {}
    const styleShowOrHide = { marginBottom: '0.618em', ...styleShowOrHideBackground }

    const buttonShowMap = (
      <Button
        key='MapFilter::showMap'
        icon={mapIcon}
        text='Show Map'
        onClick={this.handleShowMap}
        style={styleShowOrHide}
        aria-hidden={true}
      />
    )

    const buttonHideMap = (
      <Button
        key='MapFilter::hideMap'
        icon={mapIcon}
        text='Hide Map'
        onClick={this.handleHideMap}
        style={styleShowOrHide}
      />
    )

    const buttonApply = (
      <Button
        key='MapFilter::applyButton'
        text='Apply'
        onClick={() => console.log('MapFilter::apply clicked')}
        style={{ width: '35%' }}
      />
    )

    const buttonClear = (
      <Button
        key='MapFilter::clearButton'
        text='Clear'
        onClick={() => console.log('MapFilter::clear clicked')}
        style={{ width: '35%' }}
      />
    )

    const inputColumn = (
      <FlexColumn items={[this.props.showMap ? buttonHideMap : buttonShowMap, inputBoundingBox]} />
    )

    const excludeGlobalCheckbox = (
      <Checkbox
        label='Exclude Global Results'
        id='MapFilter::excludeGlobalCheckbox'
        onChange={this.toggleExcludeGlobalResults}
      />
    )

    return (
      <div style={styleMapFilter}>
        <p style={styleDescription}>
          Draw a bounding box on the map using the square button on the top right of the map or manually
          enter coordinates below. Use the Clear button to reset the map and text boxes.
        </p>
        <div style={{height:'1em'}} />
        {inputColumn}
        <div style={styleButtons}>
          {buttonApply}
          {buttonClear}
        </div>
        <div style={{borderBottom: '1px solid white', margin: '1em 0'}}></div>
        <h4 style={{paddingLeft: '0.308em'}}>Additional Filtering Options:</h4>
        {excludeGlobalCheckbox}
      </div>
    )
  }
}
