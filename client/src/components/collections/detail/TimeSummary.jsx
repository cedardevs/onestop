import React from 'react'
import {buildTimePeriodString} from '../../../utils/resultUtils'

export default class TimeSummary extends React.Component {
  render() {
    const {item} = this.props
    return (
      <div>
        {buildTimePeriodString(
          item.beginDate,
          item.beginYear,
          item.endDate,
          item.endYear
        )}
      </div>
    )
  }
}
