import React, {useState} from 'react'
import _ from 'lodash'
import DateTimeFilter from './standard/DateTimeFilter'
import GeologicTimeFilter from './geologic/GeologicTimeFilter'
import TabPanels from '../../common/ui/TabPanels'

import FlexRow from '../../common/ui/FlexRow'
import {exclamation_triangle, SvgIcon} from '../../common/SvgIcon'
import {FilterColors} from '../../../style/defaultStyles'

const alertStyle = alert => {
  if (_.isEmpty(alert)) {
    return {
      display: 'none',
    }
  }
  else {
    return {
      alignItems: 'center',
      justifyContent: 'center',
      color: FilterColors.TEXT,
      backgroundColor: '#f3f38e',
      borderRadius: '0.618em',
      textAlign: 'center',
      margin: '0.618em',
      fontSize: '1.15em',
      padding: '0.309em',
    }
  }
}

const TimeFilter = ({
  startDateTime,
  endDateTime,
  updateDateRange,
  removeDateRange,
  startYear,
  endYear,
  updateYearRange,
  removeYearRange,
  submit,
}) => {
  const [ alert, setAlert ] = useState('')

  const standardView = (
    <DateTimeFilter
      startDateTime={startDateTime}
      endDateTime={endDateTime}
      applyFilter={(startDate, endDate) => {
        removeYearRange()
        updateDateRange(startDate, endDate)
        submit()
        setAlert('')
      }}
      clear={() => {
        removeDateRange()
        submit()
        setAlert('')
      }}
    />
  )
  const geologicView = (
    <GeologicTimeFilter
      startYear={startYear}
      endYear={endYear}
      applyFilter={(startYear, endYear) => {
        removeDateRange()
        updateYearRange(startYear, endYear)
        submit()
        setAlert('')
      }}
      clear={() => {
        removeYearRange()
        submit()
        setAlert('')
      }}
    />
  )

  const VIEW_OPTIONS = [
    {
      value: 'standard',
      label: 'Datetime',
      view: standardView,
      description: 'Show standard date time filter.', // used in aria for 508, and title for slightly expanded instructions to sighted users on hover
    },
    {
      value: 'geologic',
      label: 'Geologic',
      view: geologicView,
      description: 'Show geologic year filter.',
    },
    // {
    //   value: 'periodic',
    //   label: 'Periodic',
    //   view: null,
    //   description: 'Show periodic time filter.',
    // },
  ]

  // manage default selection based on what's set
  let defaultSelection = null
  if (!_.isEmpty(startDateTime) || !_.isEmpty(endDateTime))
    defaultSelection = 'standard'
  else if (startYear != null || endYear != null) defaultSelection = 'geologic'

  const onSelectionChanged = selection => {
    setAlert('')
    if (
      selection == 'geologic' &&
      (!_.isEmpty(startDateTime) || !_.isEmpty(endDateTime))
    )
      setAlert(
        'Datetime filters will be automatically removed by geologic filters.'
      )
    if (selection == 'standard' && (startYear != null || endYear != null))
      setAlert(
        'Geologic filters will be automatically removed by datetime filters.'
      )
  }

  // TODO this alert is too aggressive, but I can't get polite to work better (it prevents the screen reader from announcing the selection when shifting views.) Try using Elliott's drawer to animate the warning open and use the delay time to see if that helps?
  const alertMessage = (
    <FlexRow
      key="GeologicDateFilter::InputColumn::Alert"
      style={alertStyle(alert)}
      items={[
        <SvgIcon
          key="alert::icon"
          size="1.4em"
          style={{marginLeft: '0.309em'}}
          path={exclamation_triangle}
        />,
        <div key="alert::message" role="alert">
          {alert}
        </div>,
      ]}
    />
  )
  return (
    <div>
      {alertMessage}
      <TabPanels
        name="timeFilter"
        options={VIEW_OPTIONS}
        defaultSelection={defaultSelection}
        onSelectionChanged={onSelectionChanged}
      />
    </div>
  )
}
export default TimeFilter
