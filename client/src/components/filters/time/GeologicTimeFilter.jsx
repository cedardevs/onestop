import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import FlexColumn from '../../common/ui/FlexColumn'
import Button from '../../common/input/Button'
import {Key} from '../../../utils/keyboardUtils'
import {isValidDateRange, textToNumber} from '../../../utils/inputUtils'
import {
  FilterColors,
  FilterStyles,
  SiteColors,
} from '../../../style/defaultStyles'
import FilterFieldset from '../FilterFieldset'
import DateFieldset from './DateFieldset'
import GeologicFieldset from './GeologicFieldset'
import GeologicFormatFieldset from './GeologicFormatFieldset'
import YearField from './YearField'
import GeologicPresets from './GeologicPresets'

const styleTimeFilter = {
  // TODO duplicate from DateTimeFilter
  ...FilterStyles.MEDIUM,
  ...{padding: '0.618em'},
}

const styleForm = {
  // TODO duplicate from DateTimeFilter
  display: 'flex',
  flexDirection: 'column',
}

const styleButtonRow = {
  // TODO duplicate from DateTimeFilter
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'center',
}

const styleButton = {
  // TODO duplicate from DateTimeFilter
  width: '30.9%',
  padding: '0.309em',
  margin: '0 0.309em',
  fontSize: '1.05em',
}

// const styleDate = { // TODO duplicate from DateFieldset
//   display: 'flex',
//   flexDirection: 'row',
// }
const styleLayout = {
  // TODO duplicate from DateFieldset
  margin: '2px',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'space-around',
  marginBottom: '0.25em',
}
//
const styleLabel = {
  // TODO duplicate from DateFieldset
  marginBottom: '0.25em',
}
//
const styleField = {
  width: '15em',
  margin: 0,
  padding: 0, // TODO only non-duplicate from DateFieldset // TODO sync up max length and width - they are just guesses for now
  color: FilterColors.TEXT,
  height: '2em',
  border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
  borderRadius: '0.309em',
}

const warningStyle = warning => {
  if (_.isEmpty(warning)) {
    return {
      display: 'none',
    }
  }
  else {
    return {
      color: SiteColors.WARNING,
      textAlign: 'center',
      margin: '0.75em 0 0.5em',
      fontWeight: 'bold',
      fontSize: '1.15em',
    }
  }
}

const GeologicTimeFilter = props => {
  const [ start, setStart ] = useState({year: null, valid: true})
  const [ end, setEnd ] = useState({year: null, valid: true})
  const [ format, setFormat ] = useState('CE')
  const [ dateRangeValid, setDateRangeValid ] = useState(true)
  const [ warning, setWarning ] = useState('')

  const onChange = (field, year, valid) => {
    // TODO just make 2 separate functions instead of passing both through this, then change the args to not need to send 'field'
    console.log(field, year)
    if (field == 'start') {
      setStart({year: year, valid: valid})
      setWarning('')
      setDateRangeValid(isValidDateRange(year, end))
    }
    if (field == 'end') {
      setEnd({year: year, valid: valid})
      setWarning('')
      setDateRangeValid(isValidDateRange(start, year))
    }
  }

  const clearDates = () => {
    props.removeYearRange()
    props.submit()
    setStart({year: null, valid: true})
    setEnd({year: null, valid: true})
    setDateRangeValid(true)
    setWarning('')
  }

  const applyDates = () => {
    if (!start.valid || !end.valid || !dateRangeValid) {
      setWarning(createWarning(start.valid, end.valid, dateRangeValid))
    }
    else {
      // TODO convert into CE here!
      console.log('submitting', start.year, end.year)
      props.updateYearRange(textToNumber(start.year), textToNumber(end.year))
      props.submit()
    }
  }

  const createWarning = (startValueValid, endValueValid, dateRangeValid) => {
    if (!startValueValid && !endValueValid) return 'Invalid start and end date.'
    if (!startValueValid) return 'Invalid start date.'
    if (!endValueValid) return 'Invalid end date.'
    if (!dateRangeValid) return 'Invalid date range.'
    return 'Unknown error'
  }

  const handleKeyDown = event => {
    if (event.keyCode === Key.ENTER) {
      event.preventDefault()
      applyDates()
    }
  }

  // const [year, setYear] = useState(1993) // TODO bleh
  const geoFormat = 'CE' // TODO add to redux state!
  const onFormatChange = f => {
    console.log('Format changed to f')
    // TODO do stuff!
    // TODO ie: change placeholder value to -YYYYYYYYY vs YYYYYYYY ?
  }

  const legendText = 'Geologic'
  const form = (
    <div key="GeologicDateFilterInput::all">
      <form
        style={styleForm}
        onKeyDown={handleKeyDown}
        aria-describedby="geologicTimeFilterInstructions"
      >
        <GeologicFormatFieldset
          geologicFormat={geoFormat}
          onFormatChange={onFormatChange}
        />
        <GeologicFieldset
          startYear={props.startYear}
          endYear={props.endYear}
          onDateChange={onChange}
        />
      </form>
    </div>
  )

  const buttons = (
    <div key="GeologicDateFilter::InputColumn::Buttons" style={styleButtonRow}>
      <Button
        key="TimeFilter::apply"
        text="Apply"
        title="Apply time filters"
        onClick={applyDates}
        style={styleButton}
      />
      <Button
        key="TimeFilter::clear"
        text="Clear"
        title="Clear time filters"
        onClick={clearDates}
        style={styleButton}
      />
    </div>
  )

  const warningMessage = (
    <div
      key="GeologicDateFilter::InputColumn::Warning"
      style={warningStyle(warning)}
      role="alert"
    >
      {warning}
    </div>
  )

  const presets = (
    <GeologicPresets
      startYear={props.startYear}
      endYear={props.endYear}
      updateYearRange={props.updateYearRange}
      submit={props.submit}
      styleLayout={styleLayout}
      styleLabel={styleLabel}
      styleField={styleField}
    />
  )

  // TODO no enforcement of that 'future dates are not accepted' thing here
  return (
    <div style={styleTimeFilter}>
      <fieldset style={{padding: '0.618em'}}>
        <legend id="geologicTimeFilterInstructions">
          Provide a start date, end date, or both. Future dates are not
          accepted.
        </legend>
        <FlexColumn items={[ form, buttons, warningMessage, presets ]} />
      </fieldset>
    </div>
  )
}
export default GeologicTimeFilter
