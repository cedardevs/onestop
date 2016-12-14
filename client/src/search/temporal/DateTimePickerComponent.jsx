import React from 'react'
import Calendar from 'rc-calendar'
import enUS from 'rc-calendar/lib/locale/en_US'
import DatePicker from 'rc-calendar/lib/Picker'
import TimePickerPanel from 'rc-time-picker/lib/Panel'
import moment from 'moment'
import 'rc-calendar/assets/index.css'
import 'rc-time-picker/assets/index.css'

const format = 'YYYY-MM-DD HH:mm:ss'
const now = moment()
now.locale('en-gb').utcOffset(0)

class DateTimePicker extends React.Component {

  constructor(props) {
    super(props)
    this.handleReset = this.handleReset.bind(this)
  }

  handleReset(e) {
    e.preventDefault()
    this.props.onChange(null)
  }

  render() {
    const props = this.props;
    const calendar = (
        <Calendar
            locale={enUS}
            timePicker={<TimePickerPanel />}
            disabledDate={props.disabledDate}
            showDateInput={false}
        />)

    return (
        <div className={props.style}>
          <DatePicker
              animation="slide-up"
              disabled={false}
              calendar={calendar}
              value={props.value}
              onChange={props.onChange}
          >
            { value => {
              return (
                  <input
                      className="pure-input-1-2"
                      placeholder="YYYY-MM-DD"
                      value={value && moment(value).format(format) || ''}
                      readOnly
                  />
              )
            }}
          </DatePicker>
          <button id={props.id} className={`pure-button`} onClick={this.handleReset}>Clear</button>
      </div>
    )
  }

}

export default DateTimePicker