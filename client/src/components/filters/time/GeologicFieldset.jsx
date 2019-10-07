import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import moment from 'moment/moment'

import {FilterColors, SiteColors} from '../../../style/defaultStyles'
import {ymdToDateMap, isValidDate} from '../../../utils/inputUtils'

import FilterFieldset from '../FilterFieldset'
import YearField from './YearField'
import MonthField from './MonthField'
import DayField from './DayField'

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

// const styleWrapper = {
// height: '2em',
// }

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
}) => {
  const legendText = 'Geologic' //`${_.capitalize(name)} Year:`

  const [ start, setStart ] = useState('')
  const [ end, setEnd ] = useState('')
  const [ startValid, setStartValid ] = useState(true)
  const [ endValid, setEndValid ] = useState(true)

  useEffect(
    () => {
      if (startYear != null) {
        // TODO validate that date is an integer value in string format
        setStart(startYear)
      }
      else {
        setStart('')
      }
    },
    [ startYear ] // when props date / redux store changes, update fields
  )
  useEffect(
    () => {
      if (endYear != null) {
        // TODO validate that date is an integer value in string format
        setEnd(endYear)
      }
      else {
        setEnd('')
      }
    },
    [ endYear ] // when props date / redux store changes, update fields
  )

  useEffect(
    () => {
      let validValue = true // TODO validate that a valid year integer was entered
      setStartValid(validValue) // update UI
      // valid hasn't actually been updated when we send onDateChange! sent the local variable instead
      updateStartYear(start, validValue)
    },
    [ start ]
  )
  useEffect(
    () => {
      let validValue = true // TODO validate that a valid year integer was entered
      setEndValid(validValue) // update UI
      // valid hasn't actually been updated when we send onDateChange! sent the local variable instead
      updateEndYear(end, validValue)
    },
    [ end ]
  )

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <YearField
          name="end"
          label="Start:"
          maxLength={14}
          value={start}
          onChange={e => setStart(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
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
          name="end"
          label="End:"
          maxLength={14}
          value={end}
          onChange={e => setEnd(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
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
