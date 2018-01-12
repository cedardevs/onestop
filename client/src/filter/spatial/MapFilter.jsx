import React, { Component } from 'react'
import FlexColumn from '../../common/FlexColumn'
import Button from '../../common/input/Button'
import _ from 'lodash'

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
      internalGeoJSON: null,
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
      this.setState({
        internalGeoJSON: geoJSON
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

  handleManualEntryCoords = (west, south, east, north) => {
    let constructedGeoJSON = convertBboxToGeoJson(_.toNumber(west), _.toNumber(south), _.toNumber(east), _.toNumber(north))
    this.setState({
      internalGeoJSON: constructedGeoJSON,
      warning: ''
    })
  }

  toggleExcludeGlobalResults = () => {
    this.props.toggleExcludeGlobal()
    this.props.submit()
  }

  applyGeometry = () => {
    if(this.state.internalGeoJSON) {
      this.props.handleNewGeometry(this.state.internalGeoJSON) // TODO -- need validation
      this.props.submit()
    }
    else {
      this.setState({
        warning: 'Entered coordinates are invalid. Ensure longitude coordinates are between -180 and 180, and latitude coordinates are between -90 and 90.'
      })
    }
  }

  clearGeometry = () => {
    this.props.removeGeometry()
    this.props.submit()

    this.setState({
      warning: ''
    })
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

    const inputBoundingBox = (
      <MapFilterCoordinatesInput key='MapFilter::coordInputBox' updateUpstreamCoords={this.handleManualEntryCoords} geoJSON={this.props.geoJSON}/>
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
