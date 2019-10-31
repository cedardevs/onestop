import React from 'react'
import _ from 'lodash'
import moment from 'moment/moment'

import {FilterColors, SiteColors} from '../../../../style/defaultStyles'
import { isValidDate} from '../../../../utils/inputUtils'

import FilterFieldset from '../../FilterFieldset'
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

const styleField = {
  color: FilterColors.TEXT,
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

// year, day, month, setYear, setDay, setMonth, valid, setValid,
  // const [ year, setYear ] = useState('')
  // const [ month, setMonth ] = useState('')
  // const [ day, setDay ] = useState('')
  // const [ valid, setValid ] = useState(true)

  // useEffect(
  //   () => {
  //     if (date != null) {
  //       let dateObj = moment(date).utc()
  //       console.log('setting year from external value:', date, dateObj.year().toString())
  //       setYear(dateObj.year().toString())
  //       setMonth(dateObj.month().toString())
  //       setDay(dateObj.date().toString())
  //     }
  //     else {
  //       console.log('setting year to empty str')
  //       setYear('')
  //       setMonth('')
  //       setDay('')
  //     }
  //   },
  //   [ date ] // when props date / redux store changes, update fields
  // )

  // useEffect(
  //   () => {
  //     let validValue = isValidDate(year, month, day)
  //     setValid(validValue) // update UI
  //     // valid hasn't actually been updated when we send onDateChange! sent the local variable instead
  //     console.log('changed', year, validValue, ymdToDateMap(year, month, day))
  //     onDateChange(ymdToDateMap(year, month, day), validValue)
  //   },
  //   [ year, month, day ]
  // )
  // const onChange = (year, month, day) => {
  //   onDateChange(year, month, day, isValidDate(year, month, day))
  // }

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <YearField
          name={name}
          value={date.year}
          onChange={e => date.setYear(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
        />
        <MonthField
          name={name}
          value={date.month}
          onChange={e => date.setMonth( e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
        />
        <DayField
          name={name}
          value={date.day}
          onChange={e => date.setDay(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
        />

        <div style={styleLayout}>
          <span />
          <span aria-hidden="true" style={styleInputValidity(date.valid)}>
            {date.valid ? '✓' : '✖'}
          </span>
        </div>
      </div>
    </FilterFieldset>
  )
}
export default DateFieldset
