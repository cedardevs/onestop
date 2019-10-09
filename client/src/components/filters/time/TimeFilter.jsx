import React from 'react'
import DateTimeFilter from './standard/DateTimeFilter'
import GeologicTimeFilter from './geologic/GeologicTimeFilter'

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
    return (
      <DateTimeFilter
        startDateTime={startDateTime}
        endDateTime={endDateTime}
        updateDateRange={updateDateRange}
        removeDateRange={removeDateRange}
        submit={submit}
      />
    )
    // return (
    //   <GeologicTimeFilter
    //     startYear={startYear}
    //     endYear={endYear}
    //     updateYearRange={updateYearRange}
    //     removeYearRange={removeYearRange}
    //     submit={submit}
    //   />
    // )
  }
}
