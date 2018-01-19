import React, {Component} from 'react'
import AppliedTime from './AppliedTime'

const styleAppliedTimes = {
  display: 'flex',
  flexFlow: 'row wrap',
  padding: '0 2em 1em',
}

export default class AppliedTimeFilter extends Component {
  render() {
    const {startDateTime, endDateTime, onUnselectDateTime} = this.props

    let appliedTimes = []
    if (startDateTime || endDateTime) {
      if (startDateTime) {
        appliedTimes.push(
          <AppliedTime
            key="start"
            label="After:"
            dateTime={startDateTime}
            onUnselect={() => onUnselectDateTime(null, endDateTime)}
          />
        )
      }
      if (endDateTime) {
        appliedTimes.push(
          <AppliedTime
            key="end"
            label="Before:"
            dateTime={endDateTime}
            onUnselect={() => onUnselectDateTime(startDateTime, null)}
          />
        )
      }
    }
    return <div style={styleAppliedTimes}>{appliedTimes}</div>
  }
}
