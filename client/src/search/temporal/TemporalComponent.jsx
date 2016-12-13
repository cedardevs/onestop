import React from 'react'
import ReactDOM from 'react-dom'
import { DateRange } from './TemporalActions'
import DateTimePicker from './DateTimePickerComponent'
import moment from 'moment'
import styles from './temporal.css'


class TemporalSearch extends React.Component {
  constructor(props) {
    super(props)
    this.startDateTime = props.startDateTime
    this.endDateTime = props.endDateTime
    this.disabledEndDate = this.disabledEndDate.bind(this)
    this.disabledStartDate = this.disabledStartDate.bind(this)
    this.onChange = this.onChange.bind(this)
  }

  disabledEndDate(endValue) {
    if (!endValue) {
      return false;
    }
    const startValue = this.startDateTime;
    if (!startValue) {
      return false;
    }
    return endValue.isBefore(startValue)
  }

  disabledStartDate(startValue) {
    if (!startValue) {
      return false;
    }
    const endValue = this.endDateTime;
    if (!endValue) {
      return false;
    }
    return endValue.isBefore(startValue)
  }

  onChange(field, value) {

    this.props.updateOnChange(value, field)
  }

  render() {
    return (
        <div className={styles.temporalContainer}>
          <div className={styles.temporalContent}>
            Start Date:
            <div className={styles.picker}>
              <DateTimePicker value={this.startDateTime}
                              onChange={this.onChange.bind(this, DateRange.START_DATE)}
                              disabledDate={this.disabledStartDate}/>
            </div>
            End Date:
            <div className={styles.picker}>
              <DateTimePicker value={this.endDateTime}
                              onChange={this.onChange.bind(this, DateRange.END_DATE)}
                              disabledDate={this.disabledEndDate}/>
            </div>
          </div>
        </div>
    )
  }
}

export default TemporalSearch