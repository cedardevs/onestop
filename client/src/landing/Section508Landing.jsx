import _ from 'lodash'
import React from 'react'
import PropTypes from 'prop-types'
import styles from './section508.css'
import moment from 'moment'
import Button from '../common/input/Button'
import { convertBboxToGeoJson, convertGeoJsonToBbox } from '../utils/geoUtils'

const styleButton = {
    margin: "1em 0 0 0.618em"
}

class Section508Landing extends React.Component {
  constructor(props) {
    super(props)

    this.updateFieldValue = this.updateFieldValue.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)

    this.fields = {
      queryText: {
        label: 'Search Text',
        placeholder: 'e.g. oceans',
        toQueryValue: x => x.trim(),
        toFieldValue: x => x
      },
      startDateTime: {
        label: 'Start Date',
        placeholder: 'e.g. 1940-02-01T00:00:00Z',
        toQueryValue: this.stringToIsoDate,
        toFieldValue: x => x
      },
      endDateTime: {
        label: 'End Date',
        placeholder: 'e.g. 2017-02-13T00:00:00Z',
        toQueryValue: this.stringToIsoDate,
        toFieldValue: x => x
      },
      geoJSON: {
        label: 'Bounding Box',
        placeholder: 'e.g. -180.00,-90.00,180.00,90.00 (W,S,E,N)',
        toQueryValue: convertBboxToGeoJson,
        toFieldValue: convertGeoJsonToBbox
      },
    }

    this.state = {
      fields: this.getFieldsFromProps(props),
      errors: {}
    }

    this.inputs = {} // set by ref callbacks to enabling focusing
  }

  componentWillReceiveProps(props) {
    this.setState({fields: this.getFieldsFromProps(props)})
  }

  getFieldsFromProps(props) {
    return _.mapValues(this.fields, (fieldDef, name) => fieldDef.toFieldValue(props[name]))
  }

  updateFieldValue(e) {
    const {name, value} = e.target
    const newFields = _.assign(this.state.fields, {[name]: value})
    this.setState({fields: newFields})
  }

  validateAndSubmit() {
    const results = _.reduce(this.fields, (collector, fieldDef, name) => {
      const raw = this.state.fields[name]
      if (!raw) {
        return collector
      }
      const valid = fieldDef.toQueryValue(raw)
      if (valid) {
        return _.set(collector, `values.${name}`, valid)
      }
      else {
        return _.set(collector, `errors.${name}`, `Invalid value for the ${fieldDef.label}`)
      }
    }, {values: {}, errors: {}})

    if (_.isEmpty(results.errors)) {
      this.props.updateSearch(results.values)
      this.props.submit()
    }
    else {
      this.setState({errors: results.errors})
    }
  }

  stringToIsoDate(string) {
    const parsedTime = moment(string.trim(), moment.ISO_8601)
    return parsedTime.isValid() ? parsedTime.toISOString() : undefined
  }

  // Search on Enter press
  handleKeyDown(e) {
    if (e.keyCode === 13) {
      e.preventDefault()
      this.validateAndSubmit()
    }
  }

  render() {
    const formInputs = _.map(this.fields, (fieldDef, name) => <div className={styles.formRow} key={name}
                                                                   data-id="formRow">
          <label htmlFor={name} className={styles.formLabel}>{fieldDef.label}</label>
          <input type="text" className={styles.formInput} name={name} ref={it => this.inputs[name] = it}
                 id={name} placeholder={fieldDef.placeholder} onKeyDown={this.handleKeyDown}
                 onChange={this.updateFieldValue} value={this.state.fields[name] || ''}/>
        </div>
    )

    return (
        <div className={styles.showcase}>
          <div className={`${styles.formDiv} pure-form`}>
            <h2>Enter Search Criteria</h2>
            {this.renderErrors()}
            <form id='508-form'>
              {formInputs}
            </form>


            <Button
                text="Search"
                onClick={() => this.validateAndSubmit()}
                title={'Search'}
                style={styleButton}
            />


          </div>
          <div className={styles.accessibilityStatement}>
            <h2>Accessibility Statement</h2>
            <p>NOAA OneStop is committed to providing access to all individuals who are seeking information from our
              website. We strive to meet or exceed requirements of Section 508 of the Rehabilitation Act, as amended
              in 1998.</p>
            <p>We recognize not all pages on our site are fully accessible at this time, however our accessible site
              aims
              to meet Level AA accessibility and provides users with access to all of the same datasets. We will
              continue to make improvements across our entire site until all pages are fully compliant.</p>
            <p>If you experience any challenges while accessing parts of our site, please contact <a
                href={'mailto:ncei.info@noaa.gov'} style={{color: '#55ace4'}}>ncei.info@noaa.gov</a></p>
          </div>
        </div>
    )
  }

  renderErrors() {
    const errors = this.state.errors
    if (!_.isEmpty(errors)) {
      return <div role="alert" className={styles.errors}>
        <h4>There are {_.size(errors)} errors in the form</h4>
        <ul>
          {_.map(errors, (message, field) => <li key={field}>
            <a onClick={() => this.inputs[field].focus()}
               title={`Focus the ${this.fields[field].label} input`}>
              {message}
            </a>
          </li>)}
        </ul>
      </div>
    }
  }

}

Section508Landing.propTypes = {
  submit: PropTypes.func.isRequired,
  clearSearch: PropTypes.func.isRequired,
  updateSearch: PropTypes.func.isRequired,
  queryText: PropTypes.string,
  startDateTime: PropTypes.string,
  endDateTime: PropTypes.string,
  geoJSON: PropTypes.object
}

export default Section508Landing
