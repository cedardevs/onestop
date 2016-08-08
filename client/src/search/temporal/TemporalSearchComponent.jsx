import React from 'react'
import { DateRange } from './TemporalActions'
import DayPicker, { DateUtils } from 'react-day-picker'
import styles from './temporal.css'
import ToggleDisplay from 'react-toggle-display'
import moment from 'moment'

//const TemporalSearch = ({onChange, currentDate}) => {

const currentYear = (new Date()).getFullYear()
const fromMonth = new Date(currentYear - 100, 0, 1, 0, 0)
const toMonth = new Date()
// Component will receive date, locale
function YearMonthForm({ date, onChange }) {
  const months = moment.months()

  const years = []
  for (let i = fromMonth.getFullYear(); i <= toMonth.getFullYear(); i++) {
    years.push(i)
  }

  var startDate = [
    <DateField
        key='start'
        updateOnOk
        dateFormat="YYYY-MM-DD"
        defaultValue=""
        onChange={(dateString)=>{formatAndEmit(dateString, DateRange.START_DATE)}}
    >
      <Calendar/>
    </DateField>
  ]
  const handleChange = function handleChange(e) {
    let { year, month } = e.target.form
    onChange(new Date(year.value, month.value))
    console.log("Year: " + year.value + ", Month: " + month.value)
  }

  var endDate = [
    <DateField
        key='end'
        updateOnOk
        dateFormat="YYYY-MM-DD"
        defaultValue=""
        onChange={(dateString)=>{formatAndEmit(dateString, DateRange.END_DATE)}}
    >
      <Calendar/>
    </DateField>
  ]
  return (
    <form className="DayPicker-Caption">
      <select name="month" onChange={ handleChange } value={ date.getMonth() }>
        { months.map((month, i) =>
          <option key={ i } value={ i }>
            { month }
          </option>)
        }
      </select>
      <select name="year" onChange={ handleChange } value={ date.getFullYear() }>
        { years.map((year, i) =>
          <option key={ i } value={ year }>
            { year }
          </option>)
        }
      </select>
    </form>
  )
}

class TemporalSearch extends React.Component {
  constructor(props) {
    super(props)
    this.handleDayClick = this.handleDayClick.bind(this)
    this.handleResetClick = this.handleResetClick.bind(this)
    this.showCurrentDate = this.showCurrentDate.bind(this)
    this.render = this.render.bind(this)
    this.state = this.getInitialState()
  }

  getInitialState() {
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
  }

  handleInputChange(e) {
    const { value } = e.target
    if (moment(value, 'L', true).isValid()) {
      this.setState({
        month: moment(value, 'L').toDate(),
        value,
      }, this.showCurrentDate)
    } else {
      this.setState({ value }, this.showCurrentDate)
    }
  }

  showCurrentDate() {
    // this.refs.daypicker.showMonth(this.state.month)
    this.setState({showCalendar: !this.state.showCalendar})
  }

  handleResetClick(e) {
    e.preventDefault()
    this.setState({
      from: null,
      to: null,
    })
  }

  render() {
    const { from, to } = this.state
    return (
      <div className={styles.temporalContainer}>
        <div>
          <p className={`${styles.dateInputs} ${styles.dateInputLeft}`}>
            <input
              ref="input"
              type="text"
              value={ this.state.from }
              placeholder="YYYY-MM-DD"
              onChange={ this.handleInputChange }
              onFocus={ this.showCurrentDate }
            />
          </p>
          <p className={`${styles.dateInputs} ${styles.dateInputRight}`}>
            <input
              ref="input"
              type="text"
              value={ this.state.to }
              placeholder="YYYY-MM-DD"
              onChange={ this.handleInputChange }
              onFocus={ this.showCurrentDate }
            />
          </p>
        </div>
        <ToggleDisplay show={this.state.showCalendar}>
          <DayPicker className={styles.dateComponent}
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
        </ToggleDisplay>
      </div>
    )
  }
}

export default TemporalSearch
