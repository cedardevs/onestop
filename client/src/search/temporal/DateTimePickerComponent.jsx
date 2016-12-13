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

  render() {
    const props = this.props;
    const calendar = (
        <Calendar
            locale={enUS}
            defaultValue={now}
            timePicker={<TimePickerPanel />}
            disabledDate={props.disabledDate}
        />)

    return (
        <DatePicker
            animation="slide-up"
            disabled={false}
            calendar={calendar}
            value={props.value}
            onChange={props.onChange}
        >
          { value => {
            return <span>
              <input
                  type="text"
                  placeholder={format}
                  value={value && moment(value).format(format) || ''}
                  readOnly
              />
            </span>
          }}
        </DatePicker>
    )
  }

}

export default DateTimePicker