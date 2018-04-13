import React, {Component} from 'react'
import FlexColumn from '../../common/FlexColumn'
import Button from '../../common/input/Button'
import _ from 'lodash'
import {Key} from '../../utils/keyboardUtils'
import mapIcon from '../../../img/font-awesome/white/svg/globe.svg'
import Checkbox from '../../common/input/Checkbox'
import {convertBboxToGeoJson, convertGeoJsonToBbox} from '../../utils/geoUtils'
import {fontFamilyMonospace} from '../../utils/styleUtils'
import Fieldset from '../Fieldset'
import {FilterTheme, SiteTheme} from '../../common/defaultStyles'

const styleMapFilter = {
  backgroundColor: FilterTheme.MEDIUM,
  padding: '0.618em',
  position: 'relative',
}

const styleDescription = {
  margin: 0,
}

const styleForm = {
  display: 'flex',
  flexDirection: 'column',
}

const styleButtons = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-around',
  marginTop: '1em',
}

const styleBreathingRoom = {
  marginTop: '1em',
}

const styleInputRow = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-between',
  margin: '0.616em 0',
  width: '15em',
}

const styleLabel = {
  width: '4em',
}

const styleCoordWrapper = {
  height: '2em',
}

const styleTextBox = {
  width: '10em',
  color: FilterTheme.TEXT,
  fontFamily: fontFamilyMonospace(),

  height: '100%',
  margin: 0,
  padding: '0 0.309em',
  border: `1px solid ${FilterTheme.LIGHT_SHADOW}`,
  borderRadius: '0.309em',
}

const styleSeparator = {
  borderBottom: `1px solid ${FilterTheme.TEXT}`,
  margin: '1em 0',
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
      warning: '',
    }
  }

  componentWillMount() {
    this.mapGeoJSONToState(this.props.geoJSON)
  }

  componentWillUnmount() {
    if (this.props.showMap) {
      this.props.toggleMap()
    }
  }

  componentWillReceiveProps(nextProps) {
    if (!nextProps.isOpen && nextProps.showMap) {
      this.props.toggleMap()
    }
    this.mapGeoJSONToState(nextProps.geoJSON)
  }

  mapGeoJSONToState = geoJSON => {
    if (geoJSON) {
      let bbox = convertGeoJsonToBbox(geoJSON)
      this.setState({
        internalGeoJSON: geoJSON,
        west: bbox.west,
        south: bbox.south,
        east: bbox.east,
        north: bbox.north,
        warning: '',
      })
    }
    else {
      this.setState(this.initialState())
    }
  }

  handleKeyDown = event => {
    if (event.keyCode === Key.ENTER) {
      event.preventDefault()
      this.applyGeometry()
    }
  }

  handleShowMap = () => {
    const {showMap, toggleMap} = this.props
    if (!showMap && toggleMap) {
      toggleMap()
    }
  }

  handleHideMap = () => {
    const {showMap, toggleMap} = this.props
    if (showMap && toggleMap) {
      toggleMap()
    }
  }

  toggleExcludeGlobalResults = () => {
    this.props.toggleExcludeGlobal()
    this.props.submit()
  }

  applyGeometry = () => {
    if (this.state.internalGeoJSON) {
      // Validation of coordinates is performed in bbox to GeoJSON conversion (geoUtils)
      this.props.handleNewGeometry(this.state.internalGeoJSON)
      this.props.submit()
    }
    else if (
      this.state.west &&
      this.state.south &&
      this.state.east &&
      this.state.north
    ) {
      this.setState({
        warning:
          'Invalid coordinates entered. Valid longitudes are between -180 and 180. Valid latitudes are between -90 and 90, where North is always greater than South.',
      })
    }
    else {
      this.setState({
        warning:
          'Incomplete coordinates entered. Ensure all four fields are populated.',
      })
    }
  }

  clearGeometry = () => {
    this.props.removeGeometry()
    this.props.submit()

    this.setState(this.initialState())
  }

  warningStyle() {
    if (_.isEmpty(this.state.warning)) {
      return {
        display: 'none',
      }
    }
    else {
      return {
        color: SiteTheme.WARNING,
        textAlign: 'center',
        margin: '0.75em 0 0.5em',
        fontWeight: 'bold',
        fontSize: '1.15em',
      }
    }
  }

  onChange(event) {
    let field = event.target.name
    let value = event.target.value
    let stateClone = {...this.state}
    stateClone[field] = value

    let {west, south, east, north} = stateClone
    let constructedGeoJSON = convertBboxToGeoJson(west, south, east, north)
    this.setState({
      [field]: value,
      internalGeoJSON: constructedGeoJSON,
      warning: '',
    })
  }

  renderInputRow = (direction, placeholderValue) => {
    let value = this.state[direction]
    let id = `MapFilterCoordinatesInput::${direction}`
    return (
      <div style={styleInputRow}>
        <label htmlFor={id} style={styleLabel}>
          {_.capitalize(direction)}
        </label>
        <div style={styleCoordWrapper}>
          <input
            type="text"
            id={id}
            name={direction}
            placeholder={placeholderValue}
            aria-placeholder={placeholderValue}
            value={value}
            style={styleTextBox}
            onChange={() => {}}
          />
        </div>
      </div>
    )
  }

  renderCoordinateInput = () => {
    return (
      <div key="MapFilterCoordinatesInput::all" style={styleBreathingRoom}>
        <form style={styleForm} onKeyDown={this.handleKeyDown}>
          <Fieldset
            onFieldsetChange={event => event => this.onChange(event)}
            legendText="Bounding Box Coordinates:"
          >
            {this.renderInputRow('west', '-180.0 to 180.0')}
            {this.renderInputRow('south', ' -90.0 to  90.0')}
            {this.renderInputRow('east', '-180.0 to 180.0')}
            {this.renderInputRow('north', ' -90.0 to  90.0')}
          </Fieldset>
        </form>
      </div>
    )
  }

  render() {
    const showMapText = this.props.showMap ? 'Hide Map' : 'Show Map'

    const buttonShowMap = (
      <Button
        key="MapFilter::showMapToggle"
        icon={mapIcon}
        text={showMapText}
        title={showMapText}
        onClick={() => {
          this.props.showMap ? this.handleHideMap() : this.handleShowMap()
        }}
        style={styleBreathingRoom}
        ariaExpanded={this.props.showMap}
      />
    )

    const buttonApply = (
      <Button
        key="MapFilter::applyButton"
        text="Apply"
        title="Apply location filter"
        onClick={this.applyGeometry}
        style={{width: '35%'}}
      />
    )

    const buttonClear = (
      <Button
        key="MapFilter::clearButton"
        text="Clear"
        title="Clear location filter"
        onClick={this.clearGeometry}
        style={{width: '35%'}}
      />
    )

    const inputBoundingBox = this.renderCoordinateInput()

    const inputColumn = (
      <FlexColumn
        items={[
          inputBoundingBox,
          <div key="MapFilter::InputColumn::Buttons" style={styleButtons}>
            {buttonApply}
            {buttonClear}
          </div>,
          <div
            key="MapFilter::InputColumn::Warning"
            style={this.warningStyle()}
            role="alert"
          >
            {this.state.warning}
          </div>,
          buttonShowMap,
        ]}
      />
    )

    const excludeGlobalCheckbox = (
      <Checkbox
        label="Exclude Global Results"
        id="MapFilter::excludeGlobalCheckbox"
        checked={!!this.props.excludeGlobal}
        onChange={this.toggleExcludeGlobalResults}
      />
    )

    return (
      <div style={styleMapFilter}>
        <p style={styleDescription}>
          Enter coordinates below or draw on the map. Use the Clear button to
          reset the map and text boxes.
        </p>
        {inputColumn}
        <div style={styleSeparator} />
        <h3 style={{paddingLeft: '0.308em'}}>Additional Filtering Options:</h3>
        {excludeGlobalCheckbox}
      </div>
    )
  }
}
