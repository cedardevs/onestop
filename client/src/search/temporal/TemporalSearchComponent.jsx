import React from 'react'
import { DateRange } from './TemporalActions'
import DayPicker, { DateUtils } from 'react-day-picker'
import moment from 'moment'
import styles from './temporal.css'
import ToggleDisplay from 'react-toggle-display'
import YearMonthForm from './YearMonthForm'

const currentYear = (new Date()).getFullYear()
const fromMonth = new Date(currentYear - 100, 0, 1, 0, 0)
const toMonth = new Date()

//const TemporalSearch = ({onChange, currentDate}) => {
class TemporalSearch extends React.Component {
  constructor(props) {
    super(props)
    this.handleDayClick = this.handleDayClick.bind(this)
    this.handleResetClick = this.handleResetClick.bind(this)
    this.showCurrentDate = this.showCurrentDate.bind(this)
    this.emitRange = this.emitRange.bind(this)
    this.render = this.render.bind(this)
    this.state = this.initialState()
  }

  initialState() {
    return {
      from: null,
      to: null,
      initialMonth: toMonth,
      showCalendar: false
    }
  }

  handleDayClick(e, day) {
    const range = DateUtils.addDayToRange(day, this.state)
    this.setState(range)
    this.emitRange(range.from, range.to)
  }

  showCurrentDate() {
    // this.refs.daypicker.showMonth(this.state.month)
    this.setState({showCalendar: !this.state.showCalendar})
  }

  handleResetClick(e) {
    e.preventDefault()
    this.setState({
      from: null,
      to: null
    })
    this.emitRange(this.setState.from, this.setState.to)
  }

  emitRange(from, to) {
    this.props.updateOnChange(from, DateRange.START_DATE)
    this.props.updateOnChange(to, DateRange.END_DATE)
  }

  render() {
    const { from, to } = this.state
    return (
      <div>
        <div>
          <div className={styles.dateInputLeft}>
            <input
              ref="input"
              type="text"
              value={ moment(this.state.from).format('L') }
              placeholder="MM-DD-YYYY"
              onFocus={ this.showCurrentDate }
            />
          </div>
          <div className={styles.dateInputRight} >
            <input
              ref="input"
              type="text"
              value={ moment(this.state.to).format('L') }
              placeholder="MM-DD-YYYY"
              onFocus={ this.showCurrentDate }
            />
          </div>
        </div>
        <ToggleDisplay show={this.state.showCalendar}>
          <div className={styles.calendarBox}>
            <DayPicker
              ref="daypicker"
              onDayClick={ this.handleDayClick }
              initialMonth={ this.state.initialMonth }
              fromMonth={ fromMonth }
              toMonth={ toMonth }
              selectedDays={ day => DateUtils.isDayInRange(day, { from, to }) }
              captionElement={
                <YearMonthForm onChange={ initialMonth => this.setState({ initialMonth }) } />
              }
            />
            <div className={styles.resetSelection}>
              <button href="#" onClick={ this.handleResetClick }><strong>Reset</strong></button>
            </div>
          </div>
        </ToggleDisplay>
      </div>
    )
  }
}

export default TemporalSearch
