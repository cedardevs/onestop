import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import {FilterColors, SiteColors} from '../../../../style/defaultStyles'

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
  width: '8em', // only non-duplicate from DateFieldset
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

const GeologicFieldset = ({start, end, format}) => {
  const legendText = 'Geologic'

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <YearField
          name="startyear"
          label="Start:"
          maxLength={14}
          value={start.year}
          valid={start.valid}
          onChange={e => start.setYear(e.target.value)}
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
          <span aria-hidden="true" style={styleInputValidity(start.valid)}>
            {start.valid ? '✓' : '✖'}
          </span>
        </div>
      </div>
      <div style={styleDate}>
        <YearField
          name="endyear"
          label="End:"
          maxLength={14}
          value={end.year}
          valid={end.valid}
          onChange={e => end.setYear(e.target.value)}
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
          <span aria-hidden="true" style={styleInputValidity(end.valid)}>
            {end.valid ? '✓' : '✖'}
          </span>
        </div>
      </div>
    </FilterFieldset>
  )
}
export default GeologicFieldset
