import React from 'react'
import _ from 'lodash'

import {FilterColors, SiteColors} from '../../../../style/defaultStyles'

import FilterFieldset from '../../FilterFieldset'
import YearField from './YearField'
import MonthField from './MonthField'
import DayField from './DayField'
import TimeField from './TimeField'

const styleDate = {
  display: 'flex',
  flexDirection: 'row',
  flexWrap: 'wrap',
  justifyContent: 'center',
  maxWidth: '14.727em',
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

const styleLabelInvalid = {
  textDecoration: `underline wavy ${SiteColors.WARNING}`,
}

const styleRequiredIndicator = {
  color: `${SiteColors.WARNING}`,
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
    width: '1em',
    color: isValid ? SiteColors.VALID : SiteColors.WARNING,
  }
}

const DateFieldset = ({
  name,
  date,
  onDateChange,
  yearErrorId,
  monthErrorId,
  dayErrorId,
  timeErrorId,
}) => {
  const legendText = `${_.capitalize(name)} Date:`

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <YearField
          name={name}
          value={date.year.value}
          required={date.year.required}
          errorId={yearErrorId}
          valid={date.year.valid}
          onChange={e => date.year.set(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleLabelInvalid={styleLabelInvalid}
          styleRequiredIndicator={styleRequiredIndicator}
          styleField={styleField}
        />
        <MonthField
          name={name}
          value={date.month.value}
          required={date.month.required}
          errorId={monthErrorId}
          valid={date.month.valid}
          onChange={e => date.month.set(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleLabelInvalid={styleLabelInvalid}
          styleRequiredIndicator={styleRequiredIndicator}
          styleField={styleField}
        />
        <DayField
          name={name}
          value={date.day.value}
          required={date.day.required}
          errorId={dayErrorId}
          valid={date.day.valid}
          onChange={e => date.day.set(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleLabelInvalid={styleLabelInvalid}
          styleRequiredIndicator={styleRequiredIndicator}
          styleField={styleField}
        />
      </div>
      <div style={styleDate}>
        <TimeField
          name={name}
          value={date.time.value}
          required={date.time.required}
          errorId={timeErrorId}
          valid={date.time.valid}
          onChange={e => date.time.set(e.target.value)}
          styleLayout={styleLayout}
          styleLabel={styleLabel}
          styleLabelInvalid={styleLabelInvalid}
          styleRequiredIndicator={styleRequiredIndicator}
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
