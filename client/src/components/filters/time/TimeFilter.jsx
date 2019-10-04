import React from 'react'
import DateTimeFilter from './DateTimeFilter'
import GeologicTimeFilter from './GeologicTimeFilter'

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
    console.log('? function', updateYearRange)
    return (
      <GeologicTimeFilter
        startYear={startYear}
        endYear={endYear}
        updateYearRange={updateYearRange}
        removeYearRange={removeYearRange}
        submit={submit}
      />
    )
  }
}
