import React, {useState} from 'react'
import _ from 'lodash'

import moment from 'moment/moment'
import FlexColumn from '../../common/ui/FlexColumn'
import Button from '../../common/input/Button'
import {Key} from '../../../utils/keyboardUtils'
import {isValidDateRange} from '../../../utils/inputUtils'
import {FilterStyles, SiteColors} from '../../../style/defaultStyles'
import DateFieldset from './DateFieldset'

const styleTimeFilter = {
  ...FilterStyles.MEDIUM,
  ...{padding: '0.618em'},
}

const styleForm = {
  display: 'flex',
  flexDirection: 'column',
}

const styleButtonRow = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'center',
}

const styleButton = {
  width: '30.9%',
  padding: '0.309em',
  margin: '0 0.309em',
  fontSize: '1.05em',
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

const DateTimeFilter = props => {
  const [ start, setStart ] = useState({date: {}, valid: true})
  const [ end, setEnd ] = useState({date: {}, valid: true})
  const [ dateRangeValid, setDateRangeValid ] = useState(true)
  const [ warning, setWarning ] = useState('')

  const updateStartDate = (date, valid) => {
    setStart({date: date, valid: valid})
    setWarning('')
    setDateRangeValid(isValidDateRange(date, end))
  }
  const updateEndDate = (date, valid) => {
    setEnd({date: date, valid: valid}) // TODO how does this verify it's not a future date?
    setWarning('')
    setDateRangeValid(isValidDateRange(start, date))
  }

  const clearDates = () => {
    props.removeDateRange()
    props.submit()
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

      props.updateDateRange(startDateString, endDateString)
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

  const form = (
    <div key="DateFilterInput::all">
      <form
        style={styleForm}
        onKeyDown={handleKeyDown}
        aria-describedby="timeFilterInstructions"
      >
        <DateFieldset
          name="start"
          date={props.startDateTime}
          onDateChange={updateStartDate}
        />
        <DateFieldset
          name="end"
          date={props.endDateTime}
          onDateChange={updateEndDate}
        />
      </form>
    </div>
  )

  const buttons = (
    <div key="DateFilter::InputColumn::Buttons" style={styleButtonRow}>
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
      key="DateFilter::InputColumn::Warning"
      style={warningStyle(warning)}
      role="alert"
    >
      {warning}
    </div>
  )

  return (
    <div style={styleTimeFilter}>
      <fieldset
        style={{borderColor: FilterColors.LIGHT_SHADOW, padding: '0.618em'}}
      >
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
