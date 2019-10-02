import React from 'react'
import _ from 'lodash'

import {FilterColors, SiteColors} from '../../../style/defaultStyles'

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

const DateFieldset = props => {
  // TODO can we move state for year month day valid values into DateFieldset?
  const {name, year, month, day, valid, onDateChange} = props
  const legendText = `${_.capitalize(name)} Date:`

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <YearField
          name={name}
          value={year}
          onChange={onDateChange}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
        />
        <MonthField
          name={name}
          value={month}
          onChange={onDateChange}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleField={styleField}
        />
        <DayField
          name={name}
          value={day}
          onChange={onDateChange}
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
