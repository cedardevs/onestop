import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import moment from 'moment/moment'
import FlexColumn from '../../../common/ui/FlexColumn'
import FlexRow from '../../../common/ui/FlexRow'
import Button from '../../../common/input/Button'
import RadioButtonSet from '../../../common/input/RadioButtonSet'
import {Key} from '../../../../utils/keyboardUtils'
import {isValidDateRange} from '../../../../utils/inputUtils'
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
import {LiveAnnouncer, LiveMessage} from 'react-aria-live'

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
  const [ warning, setWarning ] = useState('')
  const [ start ] = useDatetime('Start', startDateTime)
  const [ end ] = useDatetime('End', endDateTime)
  const [ reasonCumulative, setReasonCumulative ] = useState('')
  const [ reasonIndividual, setReasonIndividual] = useState('')

  useEffect(
    () => {
      // reset external validation each time *any* field changes, since old validation no longer applies
      start.setValidExternal(true)
      end.setValidExternal(true)
      setReasonCumulative('') // TODO end and start date same (ie 2019/12/2 for both) is valid
    },
    [ start.asMap, end.asMap ]
  )

  useEffect(
    () => {
      let errors = new Array()
      console.log(start, end)
      start.errors.year.forEach((err, index) => {
        errors.push( `start year ${err}`)
      })
      start.errors.month.forEach((err, index) => {
        errors.push( `start month ${err}`)
      })
      start.errors.day.forEach((err, index) => {
        errors.push( `start day ${err}`)
      })
      end.errors.year.forEach((err, index) => {
        errors.push( `end year ${err}`)
      })
      end.errors.month.forEach((err, index) => {
        errors.push( `end month ${err}`)
      })
      end.errors.day.forEach((err, index) => {
        errors.push( `end day ${err}`)
      })

      setReasonIndividual(_.join(errors, ', '))
    },
    [ start.errors, end.errors ]
  )

  useEffect(
    ()=> {
      setWarning(createWarning())
    }, [reasonCumulative, reasonIndividual]
  )

  const clearDates = () => {
    clear()
    start.clear()
    end.clear()
    setDateRangeValid(true)
    setWarning('')
    setReasonCumulative('')
  }

  const applyDates = () => {
    if(start.valid && end.valid) {
      let isValid = isValidDateRange(start.asMap, end.asMap)
      if (!isValid) {
        setReasonCumulative('Invalid date range.') // TODO this isn't making it into the error display
      }
      start.setValidExternal(isValid)
      end.setValidExternal(isValid)
      if (isValid) {
        let startMap = start.asMap
        let startDateString = !_.every(startMap, _.isNull)
          ? moment(startMap).utc().startOf('day').format()
          : null
        let endMap = end.asMap
        let endDateString = !_.every(endMap, _.isNull)
          ? moment(endMap).utc().startOf('day').format()
          : null

        applyFilter(startDateString, endDateString)
      }
    }
  }

  const createWarning = () => {
    let starterrlistyear = start.errors.year.map((err, index) => {
      return <li key={index}>start year {err}</li>
    })
    let starterrlistmonth = start.errors.month.map((err, index) => {
      return <li key={index}>start month {err}</li>
    })
    let starterrlistday = start.errors.day.map((err, index) => {
      return <li key={index}>start day {err}</li>
    })
    let enderrlistyear = end.errors.year.map((err, index) => {
      return <li key={index}>end year {err}</li>
    })
    let enderrlistmonth = end.errors.month.map((err, index) => {
      return <li key={index}>end month {err}</li>
    })
    let enderrlistday = end.errors.day.map((err, index) => {
      return <li key={index}>end day {err}</li>
    })
    let rangeErr = _.isEmpty(reasonCumulative)  ? null : (<li key="range">{reasonCumulative}</li>)
    return (
      <ul>
        {rangeErr}
        {starterrlistyear}
        {starterrlistmonth}
        {starterrlistday}
        {enderrlistyear}
        {enderrlistmonth}
        {enderrlistday}
      </ul>
    )
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
        <DateFieldset name="start" date={start} />
        <DateFieldset name="end" date={end} />
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

  const illustration = relation => {
    return (
      <TimeRelationIllustration
        relation={relation}
        hasStart={!_.isEmpty(start.year.value)}
        hasEnd={!_.isEmpty(end.year.value)}
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
        <FlexColumn
          items={[
            form,
            buttons,
            <div
              key="DateFilter::InputColumn::Warning"
              style={warningStyle(warning)}
              aria-hidden={true}
            >
              {warning}
            </div>,
            <LiveAnnouncer key="alert::annoucement">
              <LiveMessage message={`${reasonIndividual} ${reasonCumulative}`} aria-live="polite" />
            </LiveAnnouncer>,
          ]}
        />
      </fieldset>
      <h4 style={{margin: '0.618em 0 0.618em 0.309em'}}>
        Additional Filtering Options:
      </h4>
      {relation}
    </div>
  )
}
export default DateTimeFilter
