import React from 'react'
import DateTimeFilter from './standard/DateTimeFilter'
import GeologicTimeFilter from './geologic/GeologicTimeFilter'
import TimeSwitcher from './TimeSwitcher'

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
    return <TimeSwitcher standard={standardView} geologic={geologicView} />
  }
}
