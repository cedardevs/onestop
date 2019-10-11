import React, {useState, useEffect} from 'react'
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

const alertStyle = alert => {
  // TODO duplicated between filters!
  if (_.isEmpty(alert)) {
    return {
      display: 'none',
    }
  }
  else {
    return {
      alignItems: 'center', // TODO make the box styling (curved corners) and padding/margins match the fieldset better
      justifyContent: 'center',
      color: FilterColors.TEXT,
      backgroundColor: '#f3f38e',
      borderRadius: '0.618em',
      textAlign: 'center',
      marginBottom: '0.618em',
      fontSize: '1.15em',
      padding: '0.309em',
    }
  }
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

const DateTimeFilter = ({
  startYear,
  endYear,
  startDateTime,
  endDateTime,
  clear,
  applyFilter,
}) => {
  const [ start, setStart ] = useState({date: {}, valid: true})
  const [ end, setEnd ] = useState({date: {}, valid: true})
  const [ dateRangeValid, setDateRangeValid ] = useState(true)
  const [ warning, setWarning ] = useState('')
  const [ alert, setAlert ] = useState('')

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

  useEffect(
    () => {
      if (
        startYear != null ||
        endYear != null ||
        !_.isEmpty(startYear) ||
        !_.isEmpty(endYear) // TODO make sure this isn't confused by ints vs strings
      )
        setAlert(
          'Geologic filters are automatically removed by datetime filters.'
        )
      else setAlert('')
    },
    [ startYear, endYear ]
  )

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

  const buttons = (
    // TODO annoying inconsistency in keys: DateFilter vs TimeFilter
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

  const alertMessage = ( // TODO duplicated!
    <FlexRow
      key="TimeFilter::InputColumn::Alert"
      style={alertStyle(alert)}
      role="alert"
      items={[
        <SvgIcon
          key="alert::icon"
          size="1.4em"
          style={{marginLeft: '0.309em'}}
          path={exclamation_triangle}
        />,
        <span key="alert::message">{alert}</span>,
      ]}
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
    <div style={styleTimeFilter}>
      <fieldset
        style={{borderColor: FilterColors.LIGHT_SHADOW, padding: '0.618em'}}
      >
        <legend id="timeFilterInstructions">
          Provide a start date, end date, or both. Day and month are optional.
          Future dates are not accepted.
        </legend>
        <FlexColumn items={[ alertMessage, form, buttons, warningMessage ]} />
      </fieldset>
    </div>
  )
}
export default DateTimeFilter
