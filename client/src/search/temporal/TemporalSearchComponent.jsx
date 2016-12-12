import React from 'react'
import { DateRange } from './TemporalActions'
import DayPicker, { DateUtils } from 'react-day-picker'
import moment from 'moment'
import ReactDOM from 'react-dom'
import styles from './temporalOld.css'
import ToggleDisplay from 'react-toggle-display'
import YearMonthForm from './YearMonthForm'
import _ from 'lodash'

const currentMonth = new Date()

class TemporalSearchOld extends React.Component {
  constructor(props) {
    super(props)
    this.startDateTime = props.startDateTime
    this.endDateTime = props.endDateTime
    this.handleDayClick = this.handleDayClick.bind(this)
    this.handleResetClick = this.handleResetClick.bind(this)
    this.handleInputChange = this.handleInputChange.bind(this)
    this.showCurrentDate = this.showCurrentDate.bind(this)
    this.formatAndUpdate = this.formatAndUpdate.bind(this)
    this.toggleCalendar = this.toggleCalendar.bind(this)
    this.handleClick = this.handleClick.bind(this)
    this.render = this.render.bind(this)
    this.state = this.initialState()
  }

  initialState() {
    return {
      from: '',
      to: '',
      fromTemp: '',
      toTemp: '',
      initialMonth: currentMonth,
      showCalendar: false
    }
  }

  componentDidMount() {
    this.setState({
      from: this.startDateTime ? moment(this.startDateTime).toDate() : '',
      to: this.endDateTime ? moment(this.endDateTime).toDate() : '',
      fromTemp: this.startDateTime ? moment(this.startDateTime).format('L') : '',
      toTemp: this.endDateTime ? moment(this.endDateTime).format('L') : ''
    })

  }

  componentWillMount() {
    document.addEventListener('click', this.handleClick, false);
  }

  componentWillUnmount() {
    document.removeEventListener('click', this.handleClick, false);
  }

  componentWillUpdate(nextProps){
  	this.updateDateTimes(nextProps)
  }

  updateDateTimes({startDateTime, endDateTime}) {
    let { from, to } = this.state
    // Handle search reset
    if (startDateTime === '' && from !== '') {
      this.setState({from: '', fromTemp: ''})
    }
    if (endDateTime === '' && to !== '') {
      this.setState({to: '', toTemp: ''})
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
        self.setState({ [key + 'Temp']: validDate })
        // Update store
        self.formatAndUpdate(validDate, key)
      }
    })
  }

  // Handle clicks outside of date components
  handleClick(e) {
    var component = ReactDOM.findDOMNode(this.refs.daypicker)
    if (this.state.showCalendar && !component.contains(e.target)
        && e.target.id !== 'from' && e.target.id !== 'to' && e.target.id !== 'reset'){
      this.state.showCalendar = !this.state.showCalendar
      this.forceUpdate()
    }
  }

  handleResetClick(e) {
    e.preventDefault()
    this.setState({
      from: null,
      fromTemp: "",
      to: null,
      toTemp: ""
    })
    // Reset store
    this.formatAndUpdate('', 'from')
    this.formatAndUpdate('', 'to')
  }

  handleInputChange(e) {
    const { value, id } = e.target
    if (moment(value, 'L', true).isValid()) {
      let validDate = moment(value).format('L')
      this.setState({
        [id]: moment(value).toDate(),
        [id + 'Temp']: validDate
      })
      // Update store
      this.formatAndUpdate(validDate, id)
    } else {
      this.setState({ [id + 'Temp']: value }, this.showCurrentDate)
    }
  }

  formatAndUpdate(date, id) {
    const startEndDate = (id == 'from') ? DateRange.START_DATE : DateRange.END_DATE
    const dateString = date ? moment(date).format() : date //Allow sending of empty string on reset
    this.props.updateOnChange(dateString, startEndDate)
  }

  showCurrentDate() {
    this.refs.daypicker.showMonth(currentMonth)
  }

  toggleCalendar() {
    this.state.showCalendar = !this.state.showCalendar
    this.forceUpdate()
  }

  render() {
    let inputs = ["from", "to"]
    return (
      <div>
        {inputs.map( idField => {
          return <button id={idField} className={`pure-button ${styles.timeButton}`} onClick={this.toggleCalendar}>
            <i className={`${styles.timeIcon} fa fa-clock-o fa-2x`}></i>
          </button>
        })}
        <ToggleDisplay show={this.state.showCalendar}>
          <div className={styles.calendarBox}>
            <DayPicker
              ref="daypicker"
              onDayClick={ this.handleDayClick }
              initialMonth={ this.state.initialMonth }
              selectedDays={ day => DateUtils.isDayInRange(day, this.state) }
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

export default TemporalSearchOld
