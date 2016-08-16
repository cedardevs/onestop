import React from 'react'
import { DateRange } from './TemporalActions'
import DayPicker, { DateUtils } from 'react-day-picker'
import moment from 'moment'
import ReactDOM from 'react-dom'
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
    this.toggleDate = this.toggleDate.bind(this)
    this.handleClick = this.handleClick.bind(this)
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
      showCalendar: false
    }
  }

  componentWillMount() {
    document.addEventListener('click', this.handleClick, false);
  }

  componentWillUnmount() {
    document.removeEventListener('click', this.handleClick, false);
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

  // Handle clicks outside of date components
  handleClick(e) {
    var component = ReactDOM.findDOMNode(this.refs.daypicker)
    if (this.state.showCalendar && !component.contains(e.target)
        && e.srcElement.id !== 'from' && e.srcElement.id !== 'to' && e.srcElement.id !== 'reset'){
      this.toggleDate()
    }
  }

  handleResetClick(e) {
    e.preventDefault()
    this.setState({
      from: null,
      fromString: "",
      to: null,
      toString: ""
    })
    // Reset store
    this.formatAndEmit('', 'from')
    this.formatAndEmit('', 'to')
  }

  handleInputChange(e) {
    const { value, id } = e.target

    // If a valid date, promote state date objs, else allow string update only
    if (moment(value, 'L', true).isValid()) {
      let validDate = moment(value).format('L')
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
    const dateString = date ? moment(date).format() : date //Allow sending of empty string on reset
    this.props.updateOnChange(dateString, startEndDate)
  }

  showCurrentDate() {
    this.refs.daypicker.showMonth(currentMonth)
  }

  toggleDate() {
    this.state.showCalendar = !this.state.showCalendar
    this.forceUpdate()
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
              onFocus={ () => {
                  if (!this.state.showCalendar){
                    this.toggleDate()
                  }
                }
              }
            />
          </div>
          <div className={styles.dateInputRight} >
            <input
              type="text"
              id="to"
              value={ toString }
              placeholder="MM-DD-YYYY"
              onChange={this.handleInputChange}
              onFocus={ () => {
                  if (!this.state.showCalendar){
                    this.toggleDate()
                  }
                }
              }
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
            <div id="reset" className={styles.resetSelection}>
              <button href="#" onClick={ this.handleResetClick }><strong id="reset">Reset</strong></button>
              <button href="#" onClick={ this.showCurrentDate }><strong id="reset">Today</strong></button>
            </div>
          </div>
        </ToggleDisplay>
      </div>
    )
  }
}

export default TemporalSearch
