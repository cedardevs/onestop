import React, {Component} from 'react'
import moment from 'moment'
import _ from 'lodash'
import Button from '../../common/input/Button'
import {Key} from '../../utils/keyboardUtils'
import {
  ymdToDateMap,
  isValidDate,
  isValidDateRange,
} from '../../utils/inputUtils'

const styleInputValidity = isValid => {
  return {
    paddingLeft: '5px',
    color: isValid ? 'lime' : '#b00101',
  }
}

const styleTimeFilter = {
  backgroundColor: '#3D97D2',
  padding: '0.618em',
  color: '#F9F9F9',
}

const styleForm = {
  display: 'flex',
  flexDirection: 'column',
}

const styleFieldset = {
  alignSelf: 'center',
  marginBottom: '1em',
  border: '1px solid white',
  padding: '0.309em',
}

const styleLegend = {
  color: 'inherit',
}

const styleDate = {
  display: 'flex',
  flexDirection: 'row',
}

const styleField = {
  margin: '2px',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'space-around',
  marginBottom: '0.25em',
}

const styleLabel = {
  marginBottom: '0.25em',
}

const styleYearWrapper = {
  height: '2em',
}

const styleMonthWrapper = {
  height: '2em',
}

const styleDayWrapper = {
  height: '2em',
}

const styleYear = {
  width: '3.25em',
  color: 'black',
  height: '100%',
  margin: 0,
  padding: '0 0.309em',
  border: 'none',
  borderRadius: '0.309em',
}

const styleMonth = {
  width: '7.25em',
  color: 'black',
  height: '100%',
  margin: 0,
  padding: 0,
  border: 'none',
}
const styleDay = {
  width: '1.75em',
  color: 'black',
  height: '100%',
  margin: 0,
  padding: '0 0.309em',
  border: 'none',
  borderRadius: '0.309em',
}

const styleButtonRow = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-around',
  marginBottom: '0.5em',
}

const styleButton = {
  width: '35%',
}

export default class TimeFilter extends Component {
  constructor(props) {
    super(props)

    this.state = this.initialState()
  }

  initialState() {
    return {
      startDateYear: '',
      startDateMonth: '',
      startDateDay: '',
      endDateYear: '',
      endDateMonth: '',
      endDateDay: '',
      startValueValid: true,
      endValueValid: true,
      dateRangeValid: true,
      warning: '',
    }
  }

  componentWillMount() {
    this.mapPropsToState(this.props)
  }

  componentWillReceiveProps(nextProps) {
    this.mapPropsToState(nextProps)
  }

  mapPropsToState = props => {
    let startDate = moment(props.startDateTime).utc()
    let endDate = moment(props.endDateTime).utc()

    let startDateGiven = startDate.isValid()
    let endDateGiven = endDate.isValid()

    // Set fields as strings to avoid incorrect falsey in isValidDate if any fields are changed (January == 0 for moments)
    this.setState({
      startDateYear: startDateGiven
        ? startDate.year().toString()
        : this.initialState().startDateYear,
      startDateMonth: startDateGiven
        ? startDate.month().toString()
        : this.initialState().startDateMonth,
      startDateDay: startDateGiven
        ? startDate.date().toString()
        : this.initialState().startDateDay,
      endDateYear: endDateGiven
        ? endDate.year().toString()
        : this.initialState().endDateYear,
      endDateMonth: endDateGiven
        ? endDate.month().toString()
        : this.initialState().endDateMonth,
      endDateDay: endDateGiven
        ? endDate.date().toString()
        : this.initialState().endDateDay,
    })
  }

  warningStyle() {
    if (_.isEmpty(this.state.warning)) {
      return {
        display: 'none',
      }
    }
    else {
      return {
        color: '#b00101',
        textAlign: 'center',
        margin: '0.75em 0 0.5em',
        fontWeight: 'bold',
        fontSize: '1.15em',
      }
    }
  }

  onChange = (field, value) => {
    let stateClone = {...this.state}
    stateClone[field] = value

    this.setState({
      [field]: value,
      warning: '',
      startValueValid: isValidDate(
        stateClone.startDateYear,
        stateClone.startDateMonth,
        stateClone.startDateDay
      ),
      endValueValid: isValidDate(
        stateClone.endDateYear,
        stateClone.endDateMonth,
        stateClone.endDateDay
      ),
      dateRangeValid: isValidDateRange(
        ymdToDateMap(
          stateClone.startDateYear,
          stateClone.startDateMonth,
          stateClone.startDateDay
        ),
        ymdToDateMap(
          stateClone.endDateYear,
          stateClone.endDateMonth,
          stateClone.endDateDay
        )
      ),
    })
  }

  clearDates = () => {
    this.props.removeDateRange()
    this.props.submit()

    this.setState(this.initialState())
  }

  createWarning = (startValueValid, endValueValid, dateRangeValid) => {
    if (!startValueValid && !endValueValid) return 'Invalid start and end date.'
    if (!startValueValid) return 'Invalid start date.'
    if (!endValueValid) return 'Invalid end date.'
    if (!dateRangeValid) return 'Invalid date range.'
    return 'Unknown error'
  }

  applyDates = () => {
    const {startValueValid, endValueValid, dateRangeValid} = this.state
    if (!startValueValid || !endValueValid || !dateRangeValid) {
      this.setState({
        warning: this.createWarning(
          startValueValid,
          endValueValid,
          dateRangeValid
        ),
      })
    }
    else {
      let startDate = ymdToDateMap(
        this.state.startDateYear,
        this.state.startDateMonth,
        this.state.startDateDay
      )
      let endDate = ymdToDateMap(
        this.state.endDateYear,
        this.state.endDateMonth,
        this.state.endDateDay
      )

      let startDateString = !_.every(startDate, _.isNull)
        ? moment(startDate).utc().startOf('day').format()
        : null
      let endDateString = !_.every(endDate, _.isNull)
        ? moment(endDate).utc().startOf('day').format()
        : null

      this.props.updateDateRange(startDateString, endDateString)
      this.props.submit()
    }
  }

  createApplyButton = () => {
    return (
      <Button
        key="TimeFilter::apply"
        text="Apply"
        title="Apply time filters"
        onClick={this.applyDates}
        style={styleButton}
      />
    )
  }

  createClearButton = () => {
    return (
      <Button
        key="TimeFilter::clear"
        text="Clear"
        title="Clear time filters"
        onClick={this.clearDates}
        style={styleButton}
      />
    )
  }

  createYearField = (name, value) => {
    const id = `${name}DateYear`
    const label = `year ${name}`
    return (
      <div style={styleField}>
        <label style={styleLabel} htmlFor={id}>
          Year
        </label>
        <div style={styleYearWrapper}>
          <input
            type="text"
            id={id}
            name={id}
            placeholder="YYYY"
            aria-placeholder="Y Y Y Y"
            value={value}
            maxLength="4"
            style={styleYear}
            aria-label={label}
          />
        </div>
      </div>
    )
  }

  createMonthField = (name, value) => {
    const id = `${name}DateMonth`
    const label = `month ${name}`
    return (
      <div style={styleField}>
        <label style={styleLabel} htmlFor={id}>
          Month
        </label>
        <div style={styleMonthWrapper}>
          <select
            id={id}
            name={id}
            value={value}
            style={styleMonth}
            aria-label={label}
          >
            <option value="">(none)</option>
            <option value="0">January</option>
            <option value="1">February</option>
            <option value="2">March</option>
            <option value="3">April</option>
            <option value="4">May</option>
            <option value="5">June</option>
            <option value="6">July</option>
            <option value="7">August</option>
            <option value="8">September</option>
            <option value="9">October</option>
            <option value="10">November</option>
            <option value="11">December</option>
          </select>
        </div>
      </div>
    )
  }

  createDayField = (name, value) => {
    const id = `${name}DateDay`
    const label = `day ${name}`
    return (
      <div style={styleField}>
        <label style={styleLabel} htmlFor={id}>
          Day
        </label>
        <div style={styleDayWrapper}>
          <input
            type="text"
            id={id}
            name={id}
            placeholder="DD"
            aria-placeholder="D D"
            value={value}
            maxLength="2"
            style={styleDay}
            aria-label={label}
          />
        </div>
      </div>
    )
  }

  createDateFieldset = (name, year, month, day, valid) => {
    return (
      <fieldset
        style={styleFieldset}
        onChange={event => this.onChange(event.target.name, event.target.value)}
      >
        <legend style={styleLegend}>{_.capitalize(name)} Date: </legend>
        <div style={styleDate}>
          {this.createYearField(name, year)}
          {this.createMonthField(name, month)}
          {this.createDayField(name, day)}

          <div style={styleField}>
            <span />
            <span aria-hidden="true" style={styleInputValidity(valid)}>
              {valid ? '✓' : '✖'}
            </span>
          </div>
        </div>
      </fieldset>
    )
  }

  handleKeyDown = event => {
    if (event.keyCode === Key.ENTER) {
      event.preventDefault()
      this.applyDates()
    }
  }

  render() {
    const applyButton = this.createApplyButton()

    const clearButton = this.createClearButton()

    return (
      <div style={styleTimeFilter}>
        <p>
          Provide a start date, end date, or date range. Use year, year and
          month, or full dates. Future dates are not accepted.
        </p>
        <form style={styleForm} onKeyDown={this.handleKeyDown}>
          {this.createDateFieldset(
            'start',
            this.state.startDateYear,
            this.state.startDateMonth,
            this.state.startDateDay,
            this.state.startValueValid
          )}
          {this.createDateFieldset(
            'end',
            this.state.endDateYear,
            this.state.endDateMonth,
            this.state.endDateDay,
            this.state.endValueValid
          )}
        </form>
        <div style={styleButtonRow}>
          {applyButton}
          {clearButton}
        </div>
        <div style={this.warningStyle()} role="alert">
          {this.state.warning}
        </div>
      </div>
    )
  }
}
