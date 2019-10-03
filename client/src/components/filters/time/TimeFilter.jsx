import React from 'react'
import DateTimeFilter from './DateTimeFilter'

export default class TimeFilter extends React.Component {

  render() {

    const {
      startDateTime,
      endDateTime,
      updateDateRange,
      removeDateRange,
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
  }
}
