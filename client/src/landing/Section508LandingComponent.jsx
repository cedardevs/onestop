import React, { PropTypes } from 'react'
import styles from './section508.css'
import GeoValidate from 'geojson-validation'
import moment from 'moment'

class Section508LandingComponent extends React.Component {
  constructor(props) {
    super(props)

    this.updateQuery = this.updateQuery.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)
    this.updateBackground = this.updateBackground.bind(this)

    this.formFields = [
      { label: 'Search Text:', name: 'search-text', placeholder: 'e.g. ocean',
        value: 'queryString'},
      { label: 'Start Date:', name: 'start-date', placeholder: 'e.g. 1940-02-01T00:00:00Z',
        value: 'startDateTime'},
      { label: 'End Date:', name: 'end-date', placeholder: 'e.g. 2017-02-13T00:00:00Z',
        value: 'endDateTime'},
      { label: 'Bounding Box:', name: 'geometry', placeholder:
          `e.g. -180.00,-90.00,180.00,90.00 (W,S,E,N)`, value: 'geoJsonSelection' }
    ]
  }

  updateQuery(e) {
    let { name, value } = e.target
    value = value.trim()
    switch (name) {
      case 'search-text':
        this.props.updateSearchText(value)
        break
      case 'start-date':
        let startDate = ''
        if (moment(value).isValid()) { startDate = moment(value).toISOString() }
        this.props.updateDates(startDate, this.props.endDateTime || '')
        break
      case 'end-date':
        let endDate = ''
        if (moment(value).isValid()) { endDate = moment(value).toISOString() }
        this.props.updateDates(this.props.startDateTime || '', endDate)
        break
      case 'geometry':
        this.validateAndSubmitGeoJson(value)
        break
      default:
        // No action
    }
  }

  // Checks for bounding box only at this point
  validateAndSubmitGeoJson(coordString) {
    let numCoords
    const coordArray = coordString.split(',').map((x, idx)=> { numCoords = idx; return parseFloat(x) })
    const sw = [coordArray[0], coordArray[1]]
    const nw = [coordArray[0], coordArray[3]]
    const ne = [coordArray[2], coordArray[3]]
    const se = [coordArray[2], coordArray[1]]
    const coordinates = [[sw, nw, ne, se, sw]]
    let geoJSON = {
      type: 'Feature',
      properties: {},
      geometry: {
        coordinates,
        type: 'Polygon'
      }
    }
    if(coordArray.every(x=>(typeof x == 'number' && !isNaN(x)))
      && coordArray.length >= 4
      && GeoValidate.isPolygonCoor(coordinates)){
      this.props.handleNewGeometry(geoJSON)
    } else {
      this.props.removeGeometry()
    }
  }

  // Search on Enter press
  handleKeyDown(e) {
    if (e.keyCode === 13) {
      e.preventDefault()
      this.props.submit()
    }
  }

  componentDidUpdate() {
    this.updateBackground()
  }

  componentDidMount() {
    this.updateBackground()
  }

  updateBackground() {
    this.props.toggleBackgroundImage()
  }

  render() {
    const formInputs = this.formFields.map(field => {
      return <div className={styles.formRow} key={field.name}>
        <label htmlFor={field.name} className={styles.formLabel}>{field.label}</label>
        <input type="text" className={styles.formInput} name={field.name}
               id={field.name} placeholder={field.placeholder} onKeyDown={this.handleKeyDown}
               onChange={this.updateQuery} value={_.get(this.props, field.value) || ''}/>
      </div>
    })

    return <div className={`${styles.formDiv} pure-form`}>
      <h1>Search Criteria</h1>
      <form id='508-form'>
        {formInputs}
      </form>
      <button className={`${styles.button} pure-button`}
              onClick={this.props.submit}>
        Search
      </button>
      <button className={`${styles.button} pure-button`}
              onClick={(()=>{this.props.clearSearch()})}>
        Clear
      </button>
    </div>
  }

}

Section508LandingComponent.propTypes = {
  submit: PropTypes.func.isRequired,
  clearSearch: PropTypes.func.isRequired,
  updateSearchText: PropTypes.func.isRequired,
  updateDates: PropTypes.func.isRequired,
  handleNewGeometry: PropTypes.func.isRequired,
  removeGeometry: PropTypes.func.isRequired,
  queryString: PropTypes.string,
  startDateTime: PropTypes.string,
  endDateTime: PropTypes.string,
  geoJsonSelection: PropTypes.string
}

export default Section508LandingComponent
