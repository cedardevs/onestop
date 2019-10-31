import React from 'react'
import _ from 'lodash'

import {FilterColors, SiteColors} from '../../../../style/defaultStyles'

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
          onChange={e => date.setMonth(e.target.value)}
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
