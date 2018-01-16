import React, { Component } from 'react'
import FlexColumn from '../../common/FlexColumn'
import Button from '../../common/input/Button'
import _ from 'lodash'

import mapIcon from '../../../img/font-awesome/white/svg/globe.svg'
import Checkbox from "../../common/input/Checkbox"
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
      internalGeoJSON: null,
      west: '',
      south: '',
      east: '',
      north: '',
      warning: ''
    }
  }

  componentWillMount() {
    this.mapGeoJSONToState(this.props.geoJSON)
  }

  componentWillUnmount() {
    if(this.props.showMap) {
      this.props.toggleMap()
    }
  }

  componentWillReceiveProps(nextProps) {
    this.mapGeoJSONToState(nextProps.geoJSON)
  }

  mapGeoJSONToState = (geoJSON) => {
    if(geoJSON) {
      let bbox = convertGeoJsonToBbox(geoJSON)
      this.setState({
        internalGeoJSON: geoJSON,
        west: bbox.west,
        south: bbox.south,
        east: bbox.east,
        north: bbox.north,
        warning: ''
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

  applyGeometry = () => {
    if(this.state.internalGeoJSON) { // Validation of coordinates is performed in bbox to GeoJSON conversion (geoUtils)
      this.props.handleNewGeometry(this.state.internalGeoJSON)
      this.props.submit()
    }
    else if(this.state.west && this.state.south && this.state.east && this.state.north) {
      this.setState({
        warning: 'Entered coordinates are invalid. Ensure longitude coordinates are between -180 and 180, and latitude coordinates are between -90 and 90.'
      })
    }
    else {
      this.setState({
        warning: 'Entered incomplete set of coordinates. Ensure all four fields are populated. '
      })
    }
  }

  clearGeometry = () => {
    this.props.removeGeometry()
    this.props.submit()

    this.setState(this.initialState())
  }

  warningStyle() {
    if(_.isEmpty(this.state.warning)) {
      return {
        display: 'none'
      }
    }
    else {
      return {
        color: '#b00101',
        textAlign: 'center',
        margin: '0.75em 0 0.5em',
        fontWeight: 'bold',
        fontSize: '1.15em'
      }
    }
  }

  onChange(event) {
    let field = event.target.name
    let value = event.target.value
    let stateClone = { ...this.state }
    stateClone[field] = value

    let {west, south, east, north} = stateClone
    let constructedGeoJSON = convertBboxToGeoJson(_.toNumber(west), _.toNumber(south), _.toNumber(east), _.toNumber(north))
    this.setState({
      [field]: value,
      internalGeoJSON: constructedGeoJSON,
      warning: ''
    })
  }

  renderInputRow = (direction, placeholderValue) => {
    let value = this.state[direction]
    let id = `MapFilterCoordinatesInput::${direction}`
    return (
      <div style={styleInputRow}>
        <label htmlFor={id} style={styleLabel}>{_.capitalize(direction)}</label>
        <input type='text' id={id} name={direction} placeholder={placeholderValue} value={value} style={styleTextBox} />
      </div>
    )
  }

  renderCoordinateInput = () => {
    return (
      <div>
        <form>
          <fieldset onChange={(event) => this.onChange(event)}>
            <legend>Bounding Box Coordinates: </legend>
            {this.renderInputRow('west', ' -180.00')}
            {this.renderInputRow('south', ' -90.00')}
            {this.renderInputRow('east', ' 180.00')}
            {this.renderInputRow('north', ' 90.00')}
          </fieldset>
        </form>
      </div>
    )
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

    const inputBoundingBox = this.renderCoordinateInput()

    const inputColumn = (
      <FlexColumn items={[this.props.showMap ? buttonHideMap : buttonShowMap, inputBoundingBox]} />
    )

    const excludeGlobalCheckbox = (
      <Checkbox
        label='Exclude Global Results'
        id='MapFilter::excludeGlobalCheckbox'
        checked={this.props.excludeGlobal}
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
        <div style={this.warningStyle()} role='alert'>
          {this.state.warning}
        </div>
        <div style={{borderBottom: '1px solid white', margin: '1em 0'}}></div>
        <h4 style={{paddingLeft: '0.308em'}}>Additional Filtering Options:</h4>
        {excludeGlobalCheckbox}
      </div>
    )
  }
}
