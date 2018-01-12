import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'
import {convertGeoJsonToBbox} from "../../utils/geoUtils";

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

class MapFilterCoordinatesInput extends Component {

  constructor(props) {
    super(props)
    this.state = this.initialState()
  }

  initialState() {
    return {
      west: '',
      south: '',
      east: '',
      north: ''
    }
  }

  componentWillMount() {
    this.mapGeoJSONToState(null, this.props.geoJSON)
  }

  componentWillReceiveProps(nextProps) {
    this.mapGeoJSONToState(this.props.geoJSON, nextProps.geoJSON)
  }

  mapGeoJSONToState = (currentGeoJSON, incomingGeoJSON) => {
    if(!_.isEqual(currentGeoJSON, incomingGeoJSON)) {
      let bbox = convertGeoJsonToBbox(incomingGeoJSON)
      if(bbox) {
        this.setState({
          west: bbox.west,
          south: bbox.south,
          east: bbox.east,
          north: bbox.north
        })
      }
      else {
        this.setState(this.initialState())
      }
    }
  }

  onChange(event) {
    let field = event.target.name
    let value = event.target.value
    let stateClone = { ...this.state }
    stateClone[field] = value

    this.props.updateUpstreamCoords(stateClone.west, stateClone.south, stateClone.east, stateClone.north)
    this.setState({
      [field]: value
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

  render() {
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
}

MapFilterCoordinatesInput.propTypes = {
  geoJSON: PropTypes.object,
  updateUpstreamCoords: PropTypes.func.isRequired
}

export default MapFilterCoordinatesInput