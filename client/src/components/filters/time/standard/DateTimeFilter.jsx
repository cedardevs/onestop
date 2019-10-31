import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import moment from 'moment/moment'
import FlexColumn from '../../../common/ui/FlexColumn'
import FlexRow from '../../../common/ui/FlexRow'
import Button from '../../../common/input/Button'
import RadioButtonSet from '../../../common/input/RadioButtonSet'
import {Key} from '../../../../utils/keyboardUtils'
import {ymdToDateMap, isValidDate, isValidDateRange} from '../../../../utils/inputUtils'
import {SiteColors} from '../../../../style/defaultStyles'
import DateFieldset from './DateFieldset'
import {exclamation_triangle, SvgIcon} from '../../../common/SvgIcon'
import {
  styleFilterPanel,
  styleFieldsetBorder,
  styleForm,
} from '../../common/styleFilters'
import ApplyClearRow from '../../common/ApplyClearRow'
import Relation from '../../Relation'
import TimeRelationIllustration from '../TimeRelationIllustration'
import {useDatetime} from './DateTimeEffect'

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
  startDateTime,
  endDateTime,
  timeRelationship,
  updateTimeRelationship,
  clear,
  applyFilter,
}) => {
  // const [ start, setStart ] = useState({date: {year:'', month:'', day:''}, valid: true})
  const [ end, setEnd ] = useState({date: {}, valid: true})
  const [ dateRangeValid, setDateRangeValid ] = useState(true)
  const [ warning, setWarning ] = useState('')
  const [initStart, clearStart, startMap, start, setStart] = useDatetime((date)=>{
    setWarning('')
    setDateRangeValid(isValidDateRange(date, end.date))
  })
  // const [ sy, ssy] = useState('')
  // const [ sm, ssm] = useState('')
  // const [ sd, ssd] = useState('')
  // const [ sv, ssv] = useState(true)
  //
  //   useEffect(
  //     () => {
  //       let validValue = isValidDate(sy, sm, sd)
  //       ssv(validValue)
  //       setWarning('')
  //       let date = ymdToDateMap(sy, sm, sd)
  //       setDateRangeValid(isValidDateRange(date, end.date))
  //     },
  //     [ sy, sm, sd ]
  //   )
    useEffect(
      () => {
        initStart(startDateTime) // setFromString
      }, [startDateTime]
    )

// useEffect(
//   () => {
//     if (startDateTime != null) {
//       let dateObj = moment(startDateTime).utc()
//       ssy (dateObj.year().toString())
//       ssm (dateObj.month().toString())
//       ssd (dateObj.date().toString())
//       ssv(true) // TODO I think?
//       // setStart({date: ymdToDateMap(year, month, day), valid: true})
//     }
//     else {
//       // setYear('')
//       // setMonth('')
//       // setDay('')
//       // setStart({date: ymdToDateMap('', '', ''), valid: true})
//       // setWarning('') // TODO unsure about this
//       ssy('') ; ssm(''); ssd(''); ssv(true) // duplicate clear
//     }
//   },
//   [ startDateTime ] // when props date / redux store changes, update fields
// )

  // const updateStartDate = (year, month, day, valid) => {
  //   // setStart({date: date, valid: valid})
  //   let date = ymdToDateMap(year, month, day)
  //   setStart({date: date, valid: valid})
  //   setWarning('')
  //   setDateRangeValid(isValidDateRange(date, end.date)) // TODO
  // }
  const updateEndDate = (date, valid) => {
    setEnd({date: date, valid: valid})
    setWarning('')
    setDateRangeValid(isValidDateRange(start.date, date))
  }

  const clearDates = () => {
    clear()
    clearStart() // clear()
    // ssy('') ; ssm(''); ssd(''); ssv(true)
    setEnd({date: {}, valid: true})
    setDateRangeValid(true)
    setWarning('')
  }

  const applyDates = () => {
    console.log('applying dates:')
    if (!start.valid || !end.valid || !dateRangeValid) {
      setWarning(createWarning(end.valid, dateRangeValid))
    }
    else {

      let start = startMap()
      let startDateString = !_.every(start, _.isNull)
        ? moment(start).utc().startOf('day').format()
        : null
      let endDateString = !_.every(end.date, _.isNull)
        ? moment(end.date).utc().startOf('day').format()
        : null

      applyFilter(startDateString, endDateString)
    }
  }

  // const createWarning = (endValueValid, dateRangeValid) => {
  //   if (!sv && !endValueValid) return 'Invalid start and end date.'
  //   if (!sv) return 'Invalid start date.'
  //   if (!endValueValid) return 'Invalid end date.'
  //   if (!dateRangeValid) return 'Invalid date range.'
  //   return 'Unknown error'
  // }

  const handleKeyDown = event => {
    if (event.keyCode === Key.ENTER) {
      event.preventDefault()
      applyDates()
    }
  }

// <DateFieldset
//   name="end"
//   date={endDateTime}
//   onDateChange={updateEndDate}
// />
  const form = (
    <div key="DateFilterInput::all">
      <form
        style={styleForm}
        onKeyDown={handleKeyDown}
        aria-describedby="timeFilterInstructions"
      >
        <DateFieldset
          name="start"
          year={start.year}
          month={start.month}
          day={start.day}
          setYear={setStart.year}
          setMonth={setStart.month}
          setDay={setStart.day}
          valid={start.valid}
        />
      </form>
    </div>
  )

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

  const illustration = relation => {
    return (
      <TimeRelationIllustration
        relation={relation}
        hasStart={start.year != null}
        hasEnd={end.date.year != null}
      />
    )
  }

  const relation = (
    <Relation
      id="datetimeRelation"
      key="DateFilter::InputColumn::Advanced"
      relation={timeRelationship}
      onUpdate={updateTimeRelationship}
      illustration={illustration}
    />
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
      <h4 style={{margin: '0.618em 0 0.618em 0.309em'}}>
        Additional Filtering Options:
      </h4>
      {relation}
    </div>
  )
}
export default DateTimeFilter
