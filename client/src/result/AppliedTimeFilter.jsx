import React, {Component} from 'react'
import AppliedFilterBubble from './AppliedFilterBubble'

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
        const name = `After: ${startDateTime}`
        appliedTimes.push(
          <AppliedFilterBubble
            backgroundColor="#422555"
            borderColor="#7A2CAB"
            text={name}
            key="appliedFilter::start"
            onUnselect={() => onUnselectDateTime(null, endDateTime)}
          />
        )
      }
      if (endDateTime) {
        const name = `Before: ${endDateTime}`
        appliedTimes.push(
          <AppliedFilterBubble
            backgroundColor="#422555"
            borderColor="#7A2CAB"
            text={name}
            key="appliedFilter::end"
            onUnselect={() => onUnselectDateTime(startDateTime, null)}
          />
        )
      }
    }

    if (appliedTimes.length > 0) {
      return <div style={styleAppliedTimes}>{appliedTimes}</div>
    }
    return null
  }
}
