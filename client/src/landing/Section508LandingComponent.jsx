import React from 'react'
import styles from './section508.css'
import GeoValidate from 'geojson-validation'
import moment from 'moment'

class Section508LandingComponent extends React.Component {
  constructor(props) {
    super(props)
    this.search = props.submit
    this.clearSearch = props.clearSearch
    this.updateQuery = this.updateQuery.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)
    this.generateForm = this.generateForm.bind(this)
    this.state = this.initializeState()
  }

  initializeState() {
    return {
      formFields: [
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
        if (moment(value).isValid()) { startDate = value }
        this.props.updateDates(startDate, this.props.endDateTime || '')
        break
      case 'end-date':
        let endDate = ''
        if (moment(value).isValid()) { endDate = value }
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
    console.log(coordArray)
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
      this.submit()
    }
  }

  generateForm() {
    const { formFields } = this.state
    let form = formFields.map(field => {
      return <div className={styles.formRow} key={field.name}>
        <label htmlFor={field.name} className={styles.formLabel}>{field.label}</label>
        <input type="text" className={styles.formInput} name={field.name}
          id={field.name} placeholder={field.placeholder} onKeyDown={this.handleKeyDown}
          onChange={this.updateQuery} defaultValue={_.get(this.props, field.value)}/>
      </div>
    })
    const searchButton = <button className={`${styles.button} pure-button`}
      onClick={this.search}>Search</button>
    const clearButton = <button className={`${styles.button} pure-button`}
      onClick={(()=>{
        this.clearSearch()
        && this.form.childNodes.forEach(x=> x.lastChild.value = '')})}>
        Clear
      </button>

    return(
      <div className={`${styles.formDiv} pure-form`}>
        <form id='508-form' ref={form=>this.form=form}>
          {form}
        </form>
        {searchButton}
        {clearButton}
      </div>
      )
    }

  render() {
    return this.generateForm()
  }

  componentDidMount() {
    setTimeout(() => {
      window.dispatchEvent(new Event('resize'))
    }, 0)
  }
}

export default Section508LandingComponent
