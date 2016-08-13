import React from 'react'
import { DateRange } from './TemporalActions'
import DayPicker, { DateUtils } from 'react-day-picker'
import moment from 'moment'
import styles from './temporal.css'
import ToggleDisplay from 'react-toggle-display'
import YearMonthForm from './YearMonthForm'
import _ from 'lodash'

const currentYear = (new Date()).getFullYear()
const fromMonth = new Date(currentYear - 100, 0, 1, 0, 0)
const toMonth = new Date()

//const TemporalSearch = ({onChange, currentDate}) => {
class TemporalSearch extends React.Component {
  constructor(props) {
    super(props)
    this.handleDayClick = this.handleDayClick.bind(this)
    this.handleResetClick = this.handleResetClick.bind(this)
    this.handleInputChange = this.handleInputChange.bind(this)
    this.showCurrentDate = this.showCurrentDate.bind(this)
    this.emitRange = this.emitRange.bind(this)
    this.render = this.render.bind(this)
    this.state = this.initialState()
  }

  initialState() {
    return {
      from: null,
      fromString: "",
      to: null,
      toString: "",
      initialMonth: toMonth,
      showCalendar: true
    }
  }

  handleDayClick(e, day) {
    const range = DateUtils.addDayToRange(day, this.state)
    this.setState(range)
    this.emitRange(range.from, range.to)
  }

  showCurrentDate() {
    //this.refs.daypicker.showMonth(this.state.month)
  }

  handleResetClick(e) {
    e.preventDefault()
    this.setState({
      from: null,
      fromString: "",
      to: null,
      toString: ""
    })
    this.emitRange(this.setState.from, this.setState.to)
  }

  handleInputChange(e) {
    const { value, id } = e.target

    if (moment(value, 'L', true).isValid()) {
      this.setState({
        [id]: moment(value, 'L').toDate()
      })
    }
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
              id="from"
              type="text"
              value={ moment(this.state.from).format('L') }
              placeholder="MM-DD-YYYY"
              onFocus={ this.showCurrentDate }
              onChange={ this.handleInputChange }
            />
          </div>
          <div className={styles.dateInputRight} >
            <input
              id="to"
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
