import React from 'react'
import { DateRange } from './TemporalActions'
import DateTimePicker from './DateTimePickerComponent'
import styles from './temporal.css'


class TemporalSearch extends React.Component {
  constructor(props) {
    super(props)
    this.startDateTime = props.startDateTime
    this.endDateTime = props.endDateTime
    this.disabledEndDate = this.disabledEndDate.bind(this)
    this.disabledStartDate = this.disabledStartDate.bind(this)
    this.onChange = this.onChange.bind(this)
    this.updateTemporalFilters = this.updateTemporalFilters.bind(this)
    this.state = this.initialState()
  }

  initialState() {
    return {
      startValue: null,
      endValue: null
    }
  }

  componentDidMount() {
    this.setState({
      startValue: this.startDateTime ? this.startDateTime : null,
      endValue: this.endDateTime ? this.endDateTime : null
    })
  }

  disabledEndDate(endValue) {
    if (!endValue) {
      return false;
    }
    const startValue = this.state.startValue;
    if (!startValue) {
      return false;
    }
    return endValue.isBefore(startValue)
  }

  disabledStartDate(startValue) {
    if (!startValue) {
      return false;
    }
    const endValue = this.state.endValue;
    if (!endValue) {
      return false;
    }
    return endValue.isBefore(startValue)
  }

  onChange(field, value) {
    this.setState({
      [field]: value
    })
  }

  updateTemporalFilters() {
    this.props.updateOnChange(this.state.startValue, DateRange.START_DATE)
    this.props.updateOnChange(this.state.endValue, DateRange.END_DATE)
    this.props.toggleSelf()
  }

  render() {
    return (
        <div className={styles.temporalContainer}>
          <div className={`pure-form pure-g ${styles.temporalContent}`}>
            <div className={`pure-u-1 ${styles.pickerLabel}`}>Start Date:</div>
            <div className={`pure-u-1`}>
              <DateTimePicker id="startValue"
                              value={this.state.startValue}
                              onChange={this.onChange.bind(this, 'startValue')}
                              disabledDate={this.disabledStartDate}/>
            </div>
            <div className={`pure-u-1 ${styles.pickerLabel}`}>End Date:</div>
            <div className={`pure-u-1`}>
              <DateTimePicker id="endValue"
                              value={this.state.endValue}
                              onChange={this.onChange.bind(this, 'endValue')}
                              disabledDate={this.disabledEndDate}/>
            </div>
            <div className={`pure-u-1 ${styles.bottomButtonPanel}`}>
              <button className={`pure-button ${styles.cancelButton}`} onClick={this.props.toggleSelf}>Cancel</button>
              <button className={`pure-button ${styles.submitButton}`} onClick={this.updateTemporalFilters}>Apply To Search</button>
            </div>
          </div>
        </div>
    )
  }
}

export default TemporalSearch