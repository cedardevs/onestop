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

const GeologicFormatFieldset = ({geologicFormat, onFormatChange}) => {
  const legendText = 'Year Format'

  const [ format, setFormat ] = useState('CE')

  useEffect(
    () => {
      if (geologicFormat != null) {
        // TODO validate that format is 'CE' or 'BP'
        setFormat(geologicFormat)
      }
      else {
        setFormat('CE')
      }
    },
    [ geologicFormat ] // when props date / redux store changes, update fields
  )

  useEffect(
    () => {
      onFormatChange(format)
    },
    [ format ]
  )

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>
        <label htmlFor="CE">CE</label>
        <input
          type="radio"
          id="CE"
          name="format"
          value="CE"
          checked={format == 'CE'}
          onChange={e => setFormat(e.target.value)}
        />
        <label htmlFor="BP">BP (0 = 1950 CE)</label>
        <input
          type="radio"
          id="BP"
          name="format"
          value="BP"
          checked={format == 'BP'}
          onChange={e => setFormat(e.target.value)}
        />
      </div>
    </FilterFieldset>
  )
}
export default GeologicFormatFieldset
