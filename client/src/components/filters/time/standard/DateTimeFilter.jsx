import React, {useState} from 'react'
import _ from 'lodash'

import moment from 'moment/moment'
import FlexColumn from '../../../common/ui/FlexColumn'
import FlexRow from '../../../common/ui/FlexRow'
import Button from '../../../common/input/Button'
import {Key} from '../../../../utils/keyboardUtils'
import {isValidDateRange} from '../../../../utils/inputUtils'
import {
  FilterStyles,
  FilterColors,
  SiteColors,
} from '../../../../style/defaultStyles'
import DateFieldset from './DateFieldset'
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

const DateTimeFilter = ({startDateTime, endDateTime, clear, applyFilter}) => {
  const [ start, setStart ] = useState({date: {}, valid: true})
  const [ end, setEnd ] = useState({date: {}, valid: true})
  const [ dateRangeValid, setDateRangeValid ] = useState(true)
  const [ warning, setWarning ] = useState('')

  const updateStartDate = (date, valid) => {
    setStart({date: date, valid: valid})
    setWarning('')
    setDateRangeValid(isValidDateRange(date, end.date))
  }
  const updateEndDate = (date, valid) => {
    setEnd({date: date, valid: valid})
    setWarning('')
    setDateRangeValid(isValidDateRange(start.date, date))
  }

  const clearDates = () => {
    clear()
    setStart({date: {}, valid: true})
    setEnd({date: {}, valid: true})
    setDateRangeValid(true)
    setWarning('')
  }

  const applyDates = () => {
    if (!start.valid || !end.valid || !dateRangeValid) {
      setWarning(createWarning(start.valid, end.valid, dateRangeValid))
    }
    else {
      let startDateString = !_.every(start.date, _.isNull)
        ? moment(start.date).utc().startOf('day').format()
        : null
      let endDateString = !_.every(end.date, _.isNull)
        ? moment(end.date).utc().startOf('day').format()
        : null

      applyFilter(startDateString, endDateString)
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

  const form = (
    <div key="DateFilterInput::all">
      <form
        style={styleForm}
        onKeyDown={handleKeyDown}
        aria-describedby="timeFilterInstructions"
      >
        <DateFieldset
          name="start"
          date={startDateTime}
          onDateChange={updateStartDate}
        />
        <DateFieldset
          name="end"
          date={endDateTime}
          onDateChange={updateEndDate}
        />
      </form>
    </div>
  )

  // TODO annoying inconsistency in keys: DateFilter vs TimeFilter
  const buttons = (
    <ApplyClearRow
      key="DateFilter::InputColumn::Buttons"
      ariaActionDescription="time filters"
      applyAction={applyDates}
      clearAction={clearDates}
    />
  )

  const warningMessage = (
    <div
      key="DateFilter::InputColumn::Warning"
      style={warningStyle(warning)}
      role="alert"
    >
      {warning}
    </div>
  )

  return (
    <div style={styleFilterPanel}>
      <fieldset style={styleFieldsetBorder}>
        <legend id="timeFilterInstructions">
          Provide a start date, end date, or both. Day and month are optional.
          Future dates are not accepted.
        </legend>
        <FlexColumn items={[ form, buttons, warningMessage ]} />
      </fieldset>
    </div>
  )
}
export default DateTimeFilter
