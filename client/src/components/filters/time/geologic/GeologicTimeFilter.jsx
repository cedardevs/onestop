import React, {useState} from 'react'
import _ from 'lodash'

import FlexColumn from '../../../common/ui/FlexColumn'
import FlexRow from '../../../common/ui/FlexRow'
import Button from '../../../common/input/Button'
import {Key} from '../../../../utils/keyboardUtils'
import {isValidYearRange, textToNumber} from '../../../../utils/inputUtils'
import {
  FilterColors,
  FilterStyles,
  SiteColors,
} from '../../../../style/defaultStyles'
import FormSeparator from '../../FormSeparator'
import GeologicFieldset from './GeologicFieldset'
import GeologicFormatFieldset from './GeologicFormatFieldset'
import GeologicPresets from './GeologicPresets'

import {exclamation_triangle, SvgIcon} from '../../../common/SvgIcon'

import {
  styleFilterPanel,
  styleFieldsetBorder,
  styleForm,
} from '../../common/styleFilters'
import ApplyClearRow from '../../common/ApplyClearRow'

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

const GeologicTimeFilter = ({startYear, endYear, clear, applyFilter}) => {
  const [ start, setStart ] = useState({year: null, valid: true})
  const [ end, setEnd ] = useState({year: null, valid: true})
  const [ format, setFormat ] = useState('CE')
  const [ dateRangeValid, setDateRangeValid ] = useState(true)
  const [ warning, setWarning ] = useState('')

  const updateStartYear = (year, valid) => {
    setStart({year: year, valid: valid})
    setWarning('')
    setDateRangeValid(isValidYearRange(year, end.year))
  }
  const updateEndYear = (year, valid) => {
    setEnd({year: year, valid: valid})
    setWarning('')
    setDateRangeValid(isValidYearRange(start.year, year))
  }

  const clearDates = () => {
    clear()
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
      // TODO assumes value has been returned in CE always!
      applyFilter(textToNumber(start.year), textToNumber(end.year))
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

  const onFormatChange = f => {
    setFormat(f)
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
          geologicFormat={format}
          onFormatChange={onFormatChange}
        />
        <GeologicFieldset
          startYear={startYear}
          endYear={endYear}
          format={format}
          updateStartYear={updateStartYear}
          updateEndYear={updateEndYear}
        />
      </form>
    </div>
  )

  const buttons = (
    <ApplyClearRow
      key="GeologicDateFilter::InputColumn::Buttons"
      ariaActionDescription="time filters"
      applyAction={applyDates}
      clearAction={clearDates}
    />
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
    <div key="GeologicDateFilter::InputColumn::Presets">
      <FormSeparator text="OR" />

      <GeologicPresets
        startYear={startYear}
        endYear={endYear}
        applyFilter={applyFilter}
      />
    </div>
  )

  return (
    <div style={styleFilterPanel}>
      <fieldset style={styleFieldsetBorder}>
        <legend id="geologicTimeFilterInstructions">
          Provide a start year, end year, or both. Future dates are not
          accepted. Values can be entered in{' '}
          <abbr title="International System of Units">SI</abbr> (<abbr title="kiloannum">ka</abbr>,{' '}
          <abbr title="megaannum">Ma</abbr>, <abbr title="gigaannum">Ga</abbr>).
        </legend>
        <FlexColumn items={[ form, buttons, warningMessage, presets ]} />
      </fieldset>
    </div>
  )
}
export default GeologicTimeFilter
