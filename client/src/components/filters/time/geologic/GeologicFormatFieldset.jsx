import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import moment from 'moment/moment'

import {FilterColors, SiteColors} from '../../../../style/defaultStyles'

import FilterFieldset from '../../FilterFieldset'

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

const FORMAT_OPTIONS = [
  {
    value: 'CE',
    label: 'CE',
  },
  {
    value: 'BP',
    label: 'BP (0 = 1950 CE)',
  },
]

const DEFAULT_FORMAT = FORMAT_OPTIONS[0].value

const GeologicFormatFieldset = ({geologicFormat, onFormatChange}) => {
  const legendText = 'Year Format'

  const [ format, setFormat ] = useState(DEFAULT_FORMAT)

  useEffect(
    () => {
      if (geologicFormat != null) {
        // TODO validate that format is 'CE' or 'BP' (something from FORMAT_OPTIONS)
        setFormat(geologicFormat)
      }
      else {
        setFormat(DEFAULT_FORMAT)
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

  const radioButtons = []
  _.each(FORMAT_OPTIONS, option => {
    const id = `TimeFilter${option.value}`
    radioButtons.push(
      <label key={`TimeFilter::Format::Label::${option.value}`} htmlFor={id}>
        {option.label}
      </label>
    )
    radioButtons.push(
      <input
        key={`TimeFilter::Format::Input::${option.value}`}
        type="radio"
        id={id}
        name="format"
        value={option.value}
        checked={format == option.value}
        onChange={e => setFormat(e.target.value)}
      />
    )
  })

  return (
    <FilterFieldset legendText={legendText}>
      <div style={styleDate}>{radioButtons}</div>
    </FilterFieldset>
  )
}
export default GeologicFormatFieldset
