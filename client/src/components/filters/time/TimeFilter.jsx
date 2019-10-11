import React from 'react'
import DateTimeFilter from './standard/DateTimeFilter'
import GeologicTimeFilter from './geologic/GeologicTimeFilter'
import TabPanels from './TabPanels'

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
        startDateTime={startDateTime}
        endDateTime={endDateTime}
        updateDateRange={updateDateRange}
        removeDateRange={removeDateRange}
        submit={submit}
      />
    )
    const geologicView = (
      <GeologicTimeFilter
        startYear={startYear}
        endYear={endYear}
        updateYearRange={updateYearRange}
        removeYearRange={removeYearRange}
        submit={submit}
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

    return <TabPanels name="timeFilter" options={VIEW_OPTIONS} />
  }
}
