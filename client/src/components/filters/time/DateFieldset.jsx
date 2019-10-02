import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import moment from 'moment/moment'

import {FilterColors, SiteColors} from '../../../style/defaultStyles'
import {
  ymdToDateMap,
  isValidDate,
} from '../../../utils/inputUtils'

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

const DateFieldset = ({name, date, onDateChange}) => {
  const legendText = `${_.capitalize(name)} Date:`

  const [ year, setYear ] = useState('')
  const [ month, setMonth ] = useState('')
  const [ day, setDay ] = useState('')
  const [ valid, setValid ] = useState(true)

  useEffect(
    () => {
      if (date != null) {
        let dateObj = moment(date).utc()
        setYear(dateObj.year().toString())
        setMonth(dateObj.month().toString())
        setDay(dateObj.date().toString())
      }
      else {
        setYear('')
        setMonth('')
        setDay('')
      }
    },
    [ date ] // when props date / redux store changes, update fields
  )

  useEffect(
    () => {
      let validValue = isValidDate(year, month, day)
      setValid(validValue)
      // TODO valid hasn't actually been updated when we send onDateChange! sent the local variable instead
      onDateChange(name, ymdToDateMap(year, month, day), validValue)
    },
    [ year, month, day ]
  )

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <YearField
          name={name}
          value={year}
          onChange={e => setYear(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
        />
        <MonthField
          name={name}
          value={month}
          onChange={e => setMonth(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
        />
        <DayField
          name={name}
          value={day}
          onChange={e => setDay(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
        />

        <div style={styleLayout}>
          <span />
          <span aria-hidden="true" style={styleInputValidity(valid)}>
            {valid ? '✓' : '✖'}
          </span>
        </div>
      </div>
    </FilterFieldset>
  )
}
export default DateFieldset
