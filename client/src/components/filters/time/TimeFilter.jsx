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
      submit,
    } = this.props
    return (
      <GeologicTimeFilter
        startDateTime={startDateTime}
        endDateTime={endDateTime}
        updateDateRange={updateDateRange}
        removeDateRange={removeDateRange}
        submit={submit}
      />
    )
  }
}
