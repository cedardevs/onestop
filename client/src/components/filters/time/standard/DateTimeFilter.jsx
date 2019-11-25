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
  const [ dateRangeValid, setDateRangeValid ] = useState(true)
  const [ warning, setWarning ] = useState('')
  const [ start ] = useDatetime(startDateTime, date => {
    setWarning('')
    setDateRangeValid(isValidDateRange(date, end.asMap()))
  })
  const [ end ] = useDatetime(endDateTime, date => {
    setWarning('')
    setDateRangeValid(isValidDateRange(start.asMap(), date))
  })

  const clearDates = () => {
    clear()
    start.clear()
    end.clear()
    setDateRangeValid(true)
    setWarning('')
  }

  const applyDates = () => {
    if (!start.valid || !end.valid || !dateRangeValid) {
      setWarning(createWarning())
    }
    else {
      let startMap = start.asMap()
      let startDateString = !_.every(startMap, _.isNull)
        ? moment(startMap).utc().startOf('day').format()
        : null
      let endMap = end.asMap()
      let endDateString = !_.every(endMap, _.isNull)
        ? moment(endMap).utc().startOf('day').format()
        : null

      applyFilter(startDateString, endDateString)
    }
  }

  const createWarning = () => {
    if (!start.valid && !end.valid) return 'Invalid start and end date.'
    if (!start.valid) return 'Invalid start date.'
    if (!end.valid) return 'Invalid end date.'
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
              <LiveMessage message={warning} aria-live="polite" />
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
