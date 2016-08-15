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
    this.showCalendarDate = this.showCalendarDate.bind(this)
    this.emitRange = this.emitRange.bind(this)
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

  handleDayClick(e, day) {
    const range = DateUtils.addDayToRange(day, this.state)
    const self = this
    this.setState(range)
    // Convert to standard format for display
    _.forOwn(range, function(val, key){
      if (val) {
        self.setState({[key + 'String']: moment(val).format('L')})
      }
    })
    this.emitRange(range.from, range.to)
  }

  showCurrentDate() {
    this.refs.daypicker.showMonth(currentMonth)
  }

  showCalendarDate() {
    // this.refs.daypicker.showMonth(currentMonth)
    // if(!this.state.to || this.setState.from){
      this.setState({showCalendar: this.handleClick})
    // }
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

    // If a valid date, promote state date objs, else allow string update only
    if (moment(value, 'L', true).isValid()) {
      this.setState({
        [id]: moment(value, 'L').toDate(),
        [id + 'String']: moment(value).format('L')
      }, this.showCurrentDate)
    } else {
      this.setState({ [id + 'String']: value }, this.showCurrentDate)
    }
  }

  emitRange(from, to) {
    this.props.updateOnChange(from, DateRange.START_DATE)
    this.props.updateOnChange(to, DateRange.END_DATE)
  }

  handleClick(e) {
    var component = ReactDOM.findDOMNode(this.refs.daypicker)
    if (this.state.showCalendar && !component.contains(e.target)
        && e.srcElement.id !== 'from' && e.srcElement.id !== 'to' && e.srcElement.id !== 'reset'){
      this.toggleDate()
    }
  }

  componentWillMount() {
    document.addEventListener('click', this.handleClick, false);
  }

  componentWillUnmount() {
    document.removeEventListener('click', this.handleClick, false);
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
              onFocus={ this.showCalendarDate }
            />
          </div>
          <div className={styles.dateInputRight} >
            <input
              type="text"
              id="to"
              value={ toString }
              placeholder="MM-DD-YYYY"
              onChange={this.handleInputChange}
              onFocus={ this.showCalendarDate }
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
