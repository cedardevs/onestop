import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import {FilterColors, SiteColors} from '../../../../style/defaultStyles'
import {isValidYear, convertYearToCE} from '../../../../utils/inputUtils'

import FilterFieldset from '../../FilterFieldset'
import YearField from '../standard/YearField'

const styleDate = {
  display: 'flex',
  flexDirection: 'row',
}

const styleLayout = {
  margin: '2px',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'space-around',
  marginBottom: '0.25em',
}

const styleLabel = {
  marginBottom: '0.25em',
}

const styleField = {
  width: '7em', // TODO only non-duplicate from DateFieldset // TODO sync up max length and width - they are just guesses for now
  color: FilterColors.TEXT,
  // height: '100%', // TODO is getting rid of styleWrapper this way ok?
  height: '2em',
  border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
  borderRadius: '0.309em',
}

const styleInputValidity = isValid => {
  return {
    paddingLeft: '5px',
    color: isValid ? SiteColors.VALID : SiteColors.WARNING,
  }
}

const GeologicFieldset = ({
  startYear,
  endYear,
  updateStartYear,
  updateEndYear,
  format,
}) => {
  const legendText = 'Geologic' //`${_.capitalize(name)} Year:`

  const [ start, setStart ] = useState('')
  const [ end, setEnd ] = useState('')
  const [ startValid, setStartValid ] = useState(true)
  const [ endValid, setEndValid ] = useState(true)

  useEffect(
    () => {
      if (startYear != null) {
        // internal to component, values should be string. expected startYear format is integer
        if (format == 'BP') {
          setStart(convertYearToCE(`${startYear}`, 'BP')) //`${1950 - startYear}`)
        }
        else {
          setStart(`${startYear}`)
        }
      }
      else {
        setStart('')
      }
    },
    [ startYear, format ] // when props date / redux store changes, update fields
  )
  useEffect(
    () => {
      if (endYear != null) {
        // internal to component, values should be string. expected startYear format is integer
        if (format == 'BP') {
          setEnd(convertYearToCE(`${endYear}`, 'BP')) //setEnd(`${1950 - endYear}`)
        }
        else {
          setEnd(`${endYear}`)
        }
      }
      else {
        setEnd('')
      }
    },
    [ endYear, format ] // when props date / redux store changes, update fields
  )

  useEffect(
    // TODO this component or GeologicTimeFilter needs to validate start < end
    // validate start
    () => {
      let yearCE = convertYearToCE(start, format)
      let validValue = isValidYear(yearCE)
      setStartValid(validValue) // update UI
      updateStartYear(yearCE, validValue) // valid hasn't actually been updated when we send onDateChange! sent the local variable instead
    },
    [ start ]
  )

  useEffect(
    // validate end
    () => {
      let yearCE = convertYearToCE(end, format)
      let validValue = isValidYear(yearCE)
      setEndValid(validValue) // update UI
      updateEndYear(yearCE, validValue) // valid hasn't actually been updated when we send onDateChange! sent the local variable instead
    },
    [ end ]
  )

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <YearField
          name="startyear"
          label="Start:"
          maxLength={14}
          value={start}
          onChange={e => setStart(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
          placeholder={format == 'BP' ? 'YYYYYYYYY' : '-YYYYYYYYY'}
          ariaPlaceholder={
            format == 'BP' ? 'Y Y Y Y Y Y Y Y Y' : 'negative Y Y Y Y Y Y Y Y Y'
          }
        />

        <div style={styleLayout}>
          <span />
          <span aria-hidden="true" style={styleInputValidity(startValid)}>
            {startValid ? '✓' : '✖'}
          </span>
        </div>
      </div>
      <div style={styleDate}>
        <YearField
          name="endyear"
          label="End:"
          maxLength={14}
          value={end}
          onChange={e => setEnd(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
          placeholder={format == 'BP' ? 'YYYYYYYYY' : '-YYYYYYYYY'}
          ariaPlaceholder={
            format == 'BP' ? 'Y Y Y Y Y Y Y Y Y' : 'negative Y Y Y Y Y Y Y Y Y'
          }
        />

        <div style={styleLayout}>
          <span />
          <span aria-hidden="true" style={styleInputValidity(endValid)}>
            {endValid ? '✓' : '✖'}
          </span>
        </div>
      </div>
    </FilterFieldset>
  )
}
export default GeologicFieldset
