import _ from 'lodash'
import React, { PropTypes } from 'react'
import styles from './section508.css'
import GeoValidate from 'geojson-validation'
import moment from 'moment'

class Section508LandingComponent extends React.Component {
  constructor(props) {
    super(props)

    this.updateFieldValue = this.updateFieldValue.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)

    this.formFields = [
      {label: 'Search Text:', name: 'queryText', placeholder: 'e.g. ocean'},
      {label: 'Start Date:', name: 'startDateTime', placeholder: 'e.g. 1940-02-01T00:00:00Z'},
      {label: 'End Date:', name: 'endDateTime', placeholder: 'e.g. 2017-02-13T00:00:00Z'},
      {label: 'Bounding Box:', name: 'geoJson', placeholder: `e.g. -180.00,-90.00,180.00,90.00 (W,S,E,N)`}
    ]

    this.state = this.getFieldsFromProps(props)
  }

  componentWillReceiveProps(props) {
    this.setState(this.getFieldsFromProps(props))
  }

  getFieldsFromProps(props) {
    const fieldNames = _.map(this.formFields, field => field.name)
    return _.pick(props, fieldNames)
  }

  updateFieldValue(e) {
    const { name, value } = e.target
    const newState = {}
    newState[name] = value.trim()
    this.setState(newState)
  }

  validateAndSubmit() {
    const errors = []
    const fieldNames = _.map(this.formFields, field => field.name)
    const searchValues = _.reduce(fieldNames, (collector, name) => {
      const raw = this.state[name]
      const transformed = this.transformFieldToSubmit(name, raw)
      if (raw && !transformed) {
        errors.push(`Invalid value for the ${name} field`)
        return collector
      }
      return _.set(collector, name, transformed)
    }, {})

    if (errors.length > 0) {
      this.setState({errors: errors})
    }
    else {
      this.props.updateSearch(searchValues)
      this.props.submit()
    }
  }

  transformFieldToSubmit(name, value) {
    switch (name) {
      case 'startDateTime':
      case 'endDateTime':
        const parsedTime = moment(value, moment.ISO_8601)
        return parsedTime.isValid() ? parsedTime.toISOString() : undefined

      case 'geoJson':
        return this.bboxToGeoJson(value)

      case 'queryString':
      default:
        return value
    }
  }

  // Checks for bounding box only at this point
  bboxToGeoJson(coordString) {
    const coordArray = coordString.split(',').map(x => parseFloat(x))
    const sw = [coordArray[0], coordArray[1]]
    const nw = [coordArray[0], coordArray[3]]
    const ne = [coordArray[2], coordArray[3]]
    const se = [coordArray[2], coordArray[1]]
    let geoJSON = {
      type: 'Feature',
      properties: {},
      geometry: {
        coordinates: [[sw, nw, ne, se, sw]],
        type: 'Polygon'
      }
    }
    return GeoValidate.isPolygon(geoJSON.geometry) ? geoJSON : undefined;
  }

  // Search on Enter press
  handleKeyDown(e) {
    if (e.keyCode === 13) {
      e.preventDefault()
      this.validateAndSubmit()
    }
  }

  render() {
    const formInputs = this.formFields.map(field => {
      return <div className={styles.formRow} key={field.name}>
        <label htmlFor={field.name} className={styles.formLabel}>{field.label}</label>
        <input type="text" className={styles.formInput} name={field.name}
               id={field.name} placeholder={field.placeholder} onKeyDown={this.handleKeyDown}
               onChange={this.updateFieldValue} value={_.get(this.state, field.name) || ''}/>
      </div>
    })

    return <div className={`${styles.formDiv} pure-form`}>
      <h1>Search Criteria</h1>
      {/*<h2>Errors</h2>*/}
      {/*<ul>{_.map(this.state.errors, message => <li>{message}</li>)}</ul>*/}
      <form id='508-form'>
        {formInputs}
      </form>
      <button className={`${styles.button} pure-button`}
              onClick={() => this.validateAndSubmit()}>
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
