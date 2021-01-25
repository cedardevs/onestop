import React, {useState, useEffect} from 'react'
import _ from 'lodash'

import FlexColumn from '../../../common/ui/FlexColumn'
import FlexRow from '../../../common/ui/FlexRow'
import Button from '../../../common/input/Button'
import RadioButtonSet from '../../../common/input/RadioButtonSet'
import {Key} from '../../../../utils/keyboardUtils'
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
import {useDateRange} from './DateTimeEffect'
import {consolidateStyles} from '../../../../utils/styleUtils'

const warningStyle = (startValid, endValid, errorMessage) => {
  return consolidateStyles(
    {
      color: SiteColors.WARNING,
      textAlign: 'center',
      fontWeight: 'bold',
      fontSize: '1.15em',
    },
    !startValid || !endValid || !_.isEmpty(errorMessage)
      ? {margin: '0.75em 0 0.5em'}
      : null
  )
}

const DateTimeFilter = ({
  startDateTime,
  endDateTime,
  timeRelationship,
  updateTimeRelationship,
  clear,
  applyFilter,
}) => {
  const [
    start,
    end,
    clearDateRange,
    validate,
    asDateStrings,
    errorCumulative,
  ] = useDateRange(startDateTime, endDateTime)

  const applyDates = () => {
    if (validate()) {
      const [ startDateString, endDateString ] = asDateStrings()
      applyFilter(startDateString, endDateString)
    }
  }

  const clearDates = () => {
    clear()
    clearDateRange()
  }

  const renderErrors = ({field, fieldset}) => {
    return (
      <div>
        {field} {fieldset}
      </div>
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
        <DateFieldset
          name="start"
          date={start}
          yearErrorId={
            !_.isEmpty(errorCumulative) ? (
              'dateFilterRangeErrors'
            ) : (
              'dateFilterStartYearErrors'
            )
          }
          monthErrorId={
            !_.isEmpty(errorCumulative) ? (
              'dateFilterRangeErrors'
            ) : (
              'dateFilterStartMonthErrors'
            )
          }
          dayErrorId={
            !_.isEmpty(errorCumulative) ? (
              'dateFilterRangeErrors'
            ) : (
              'dateFilterStartDayErrors'
            )
          }
          timeErrorId={
            !_.isEmpty(errorCumulative) ? (
              'dateFilterRangeErrors'
            ) : (
              'dateFilterStartTimeErrors'
            )
          }
        />
        <DateFieldset
          name="end"
          date={end}
          yearErrorId={
            !_.isEmpty(errorCumulative) ? (
              'dateFilterRangeErrors'
            ) : (
              'dateFilterEndYearErrors'
            )
          }
          monthErrorId={
            !_.isEmpty(errorCumulative) ? (
              'dateFilterRangeErrors'
            ) : (
              'dateFilterEndMonthErrors'
            )
          }
          dayErrorId={
            !_.isEmpty(errorCumulative) ? (
              'dateFilterRangeErrors'
            ) : (
              'dateFilterEndDayErrors'
            )
          }
          timeErrorId={
            !_.isEmpty(errorCumulative) ? (
              'dateFilterRangeErrors'
            ) : (
              'dateFilterEndTimeErrors'
            )
          }
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
          Provide a start date, end date, or both. Day, month, and time are
          optional. Future dates are not accepted. Time should be a 24 hour
          format.
        </legend>
        <FlexColumn
          items={[
            form,
            buttons,
            <div
              key="DateFilter::InputColumn::Warning"
              style={warningStyle(start.valid, end.valid, errorCumulative)}
              role="alert"
              aria-live="polite"
            >
              <div id="dateFilterStartYearErrors">
                {renderErrors(start.year.errors)}
              </div>
              <div id="dateFilterStartMonthErrors">
                {renderErrors(start.month.errors)}
              </div>
              <div id="dateFilterStartDayErrors">
                {renderErrors(start.day.errors)}
              </div>
              <div id="dateFilterStartTimeErrors">
                {renderErrors(start.time.errors)}
              </div>
              <div id="dateFilterEndYearErrors">
                {renderErrors(end.year.errors)}
              </div>
              <div id="dateFilterEndMonthErrors">
                {renderErrors(end.month.errors)}
              </div>
              <div id="dateFilterEndDayErrors">
                {renderErrors(end.day.errors)}
              </div>
              <div id="dateFilterEndTimeErrors">
                {renderErrors(end.time.errors)}
              </div>
              <div id="dateFilterRangeErrors">
                {!_.isEmpty(errorCumulative) ? errorCumulative : null}
              </div>
            </div>,
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
