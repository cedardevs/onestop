import React from 'react'
import Calendar from 'rc-calendar'
import enUS from 'rc-calendar/lib/locale/en_US'
import DatePicker from 'rc-calendar/lib/Picker'
import TimePickerPanel from 'rc-time-picker/lib/Panel'
import moment from 'moment'
import 'rc-calendar/assets/index.css'
import 'rc-time-picker/assets/index.css'
import styles from './temporal.css'

const format = 'YYYY-MM-DD HH:mm:ss'

class DateTimePicker extends React.Component {

  constructor(props) {
    super(props)
    this.handleReset = this.handleReset.bind(this)
    this.getValueString = this.getValueString.bind(this)
  }

  handleReset(e) {
    e.preventDefault()
    this.props.onChange(null)
  }

  getValueString(value) {
    if(value.value !== null && value.value !== undefined) {
      return value.value.utc().format(format)
    } else {
      return ''
    }
  }

  render() {
    const props = this.props
    const timePicker = <TimePickerPanel
        className={styles.timePicker}
        showHour={true}
        showMinute={true}
        showSecond={true}
    />
    const calendar = <Calendar
        locale={enUS}
        defaultValue={moment().utc().hour(0).minute(0).second(0)}
        timePicker={timePicker}
        disabledDate={props.disabledDate}
        showDateInput={false}
    />
    const renderInput = (value) => <input
        className={`pure-input-2-3 ${styles.inputField}`}
        placeholder={"Choose a date"}
        value={this.getValueString(value)}
        readOnly
    />

    return (
      <div className={styles.pickerRow}>
        <DatePicker
            animation="slide-up"
            disabled={false}
            calendar={calendar}
            value={props.value}
            onChange={props.onChange}
            style={{color: "black"}}
        >
          {renderInput}
        </DatePicker>
        <button id={props.id} className={`pure-button ${styles.clearButton}`} onClick={this.handleReset}>
          <i className="fa fa-times fa-fw fa-lg"></i></button>
      </div>
    )
  }

}

export default DateTimePicker
