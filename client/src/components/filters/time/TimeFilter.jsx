import React from 'react'
import _ from 'lodash'
import DateTimeFilter from './standard/DateTimeFilter'
import GeologicTimeFilter from './geologic/GeologicTimeFilter'
import TabPanels from '../../common/ui/TabPanels'

export default class TimeFilter extends React.Component {
  render() {
    const {
      startDateTime,
      endDateTime,
      updateDateRange,
      removeDateRange,
      startYear,
      endYear,
      updateYearRange,
      removeYearRange,
      submit,
    } = this.props

    const standardView = (
      <DateTimeFilter
        startYear={startYear}
        endYear={endYear}
        startDateTime={startDateTime}
        endDateTime={endDateTime}
        applyFilter={(startDate, endDate) => {
          removeYearRange()
          updateDateRange(startDate, endDate)
          submit()
        }}
        clear={() => {
          removeDateRange()
          submit()
        }}
      />
    )
    const geologicView = (
      <GeologicTimeFilter
        startYear={startYear}
        endYear={endYear}
        startDateTime={startDateTime}
        endDateTime={endDateTime}
        applyFilter={(startYear, endYear) => {
          removeDateRange()
          updateYearRange(startYear, endYear)
          submit()
        }}
        clear={() => {
          removeYearRange()
          submit()
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

    let selected = null
    if (!_.isEmpty(startDateTime) || !_.isEmpty(endDateTime))
      selected = 'standard'
    else if (startYear != null || endYear != null) selected = 'geologic'

    return (
      <TabPanels name="timeFilter" options={VIEW_OPTIONS} selected={selected} />
    )
  }
}
