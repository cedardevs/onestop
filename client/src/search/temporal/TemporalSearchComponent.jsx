import React from 'react'
import { DateRange } from './TemporalActions'
import DayPicker, { DateUtils } from 'react-day-picker'
import moment from 'moment'
import styles from './temporal.css'
import ToggleDisplay from 'react-toggle-display'
import YearMonthForm from './YearMonthForm'
import _ from 'lodash'

const currentYear = (new Date()).getFullYear()
//TODO: Extend selection period beyond 100 years
const earliestMonth = new Date(currentYear - 100, 0, 1, 0, 0)
const currentMonth = new Date()

class TemporalSearch extends React.Component {
  constructor(props) {
    super(props)
    this.handleDayClick = this.handleDayClick.bind(this)
    this.handleResetClick = this.handleResetClick.bind(this)
    this.handleInputChange = this.handleInputChange.bind(this)
    this.showCurrentDate = this.showCurrentDate.bind(this)
    this.formatAndEmit = this.formatAndEmit.bind(this)
    this.render = this.render.bind(this)
    this.state = this.initialState()
  }

  initialState() {
    return {
      from: null,
      fromString: "",
      to: null,
      toString: "",
      initialMonth: currentMonth,
      showCalendar: true
    }
  }

  handleDayClick(e, day) {
    const range = DateUtils.addDayToRange(day, this.state)
    const self = this
    this.setState(range)
    // Convert to standard format for display
    _.forOwn(range, function(val, key){
      if (val) {
        let validDate = moment(val).format('L')
        self.setState({ [key + 'String']: validDate })
        // Update store
        self.formatAndEmit(validDate, key)
      }
    })
  }

  showCurrentDate() {
    this.refs.daypicker.showMonth(currentMonth)
  }

  handleResetClick(e) {
    e.preventDefault()
    this.setState({
      from: null,
      fromString: "",
      to: null,
      toString: ""
    })
  }

  handleInputChange(e) {
    const { value, id } = e.target

    // If a valid date, promote state date objs, else allow string update only
    if (moment(value, 'L', true).isValid()) {
      let validDate = moment(value, 'L').toDate()
      this.setState({
        [id]: moment(value, 'L').toDate(),
        [id + 'String']: validDate
      })
      // Update store
      this.formatAndEmit(validDate, id)
    } else {
      this.setState({ [id + 'String']: value }, this.showCurrentDate)
    }
  }

  formatAndEmit(date, id) {
    const startEndDate = (id == 'from') ? DateRange.START_DATE : DateRange.END_DATE
    this.props.updateOnChange(date, startEndDate)
  }

  render() {
    let { from, to, fromString, toString } = this.state
    return (
      <div>
        <div>
          <div className={styles.dateInputLeft}>
            <input
              type="text"
              id="from"
              value={ fromString }
              placeholder="MM-DD-YYYY"
              onChange={this.handleInputChange}
              onFocus={ this.showCurrentDate }
            />
          </div>
          <div className={styles.dateInputRight} >
            <input
              type="text"
              id="to"
              value={ toString }
              placeholder="MM-DD-YYYY"
              onChange={this.handleInputChange}
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
              earliestMonth={ earliestMonth }
              currentMonth={ currentMonth }
              selectedDays={ day => DateUtils.isDayInRange(day, { from, to }) }
              captionElement={
                <YearMonthForm onChange={ initialMonth => this.setState({ initialMonth }) } />
              }
            />
            <div className={styles.resetSelection}>
              <button href="#" onClick={ this.handleResetClick }><strong>Reset</strong></button>
            </div>
            <div className={styles.resetSelection}>
              <button href="#" onClick={ this.showCurrentDate }><strong>Today</strong></button>
            </div>
          </div>
        </ToggleDisplay>
      </div>
    )
  }
}

export default TemporalSearch
