import React, { Component } from 'react'
import FlexColumn from '../../common/FlexColumn'
import Button from '../../common/input/Button'

import mapIcon from '../../../img/font-awesome/white/svg/globe.svg'
import Checkbox from "../../common/input/Checkbox"
import MapFilterCoordinatesInput from "./MapFilterCoordinatesInput"
import {convertBboxToGeoJson, convertGeoJsonToBbox} from "../../utils/geoUtils";

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

const styleInputRow = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-between',
  margin: '0.616em 0',
  width: '15em'
}

const styleLabel = {
  width: '4em'
}

const styleTextBox = {
  width: '10em',
  color: 'black'
}

export default class MapFilter extends Component {

  constructor(props) {
    super(props)
    this.state = this.initialState()
  }

  initialState() {
    return {
      west: '',
      south: '',
      east: '',
      north: '',
      internalGeoJSON: null,
    }
  }

  componentWillMount() {
    this.mapPropsToState(this.props)
  }

  componentWillReceiveProps(nextProps) {
    this.mapPropsToState(nextProps)
  }

  mapPropsToState = (props) => {
    let bbox = convertGeoJsonToBbox(props.geoJSON)
    if(bbox) {
      this.setState({
        west: bbox.west,
        south: bbox.south,
        east: bbox.east,
        north: bbox.north,
        internalGeoJSON: props.geoJSON
      })
    }
    else {
      this.setState(this.initialState())
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

  onChange(field, value) {
    let stateClone = { ...this.state }
    stateClone[field] = value
    stateClone[internalGeoJSON] = convertBboxToGeoJson(stateClone.west, stateClone.south, stateClone.east, stateClone.north)

    this.setState({
      [field]: value,
      internalGeoJSON: convertBboxToGeoJson(stateClone.west, stateClone.south, stateClone.east, stateClone.north)
    })
  }

  applyGeometry = () => {
    this.props.handleNewGeometry(this.state.internalGeoJSON) // TODO -- need validation
    this.props.submit()
  }

  clearGeometry = () => {
    this.props.removeGeometry()
    this.props.submit()
  }

  render() {
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
        onClick={this.applyGeometry}
        style={{ width: '35%' }}
      />
    )

    const buttonClear = (
      <Button
        key='MapFilter::clearButton'
        text='Clear'
        onClick={this.clearGeometry}
        style={{ width: '35%' }}
      />
    )

    const coordinateEntryRow = (value, direction, placeholderValue) => {
      let id = `MapFilter::${direction}ernCoordinate`
      return (
        <div style={styleInputRow}>
          <label htmlFor={id} style={styleLabel}>{_.capitalize(direction)}</label>
          <input type='text' id={id} name={direction} placeholder={placeholderValue} value={value} style={styleTextBox} />
        </div>
      )
    }

    const inputBoundingBox = (
      <div key='MapFilter::coordinatesFieldset'>
        <form>
          <fieldset onChange={(event) => this.onChange(event.target.name, event.target.value)}>
            <legend>Bounding Box Coordinates: </legend>
            {coordinateEntryRow(this.state.west, 'west', ' -180.00')}
            {coordinateEntryRow(this.state.south, 'south', ' -90.00')}
            {coordinateEntryRow(this.state.east, 'east', ' 180.00')}
            {coordinateEntryRow(this.state.north, 'north', ' 90.00')}
          </fieldset>
        </form>
      </div>
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
