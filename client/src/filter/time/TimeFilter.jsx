import React, {Component} from 'react'
import moment from 'moment'
import _ from 'lodash'
import Button from '../../common/input/Button'

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

const styleFieldset = {
  marginBottom: '1em',
  width: '15em',
}

const styleLabels = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-around',
  marginBottom: '0.25em',
}

const styleInputs = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-around',
  color: 'black',
}

const styleYear = {
  width: '3.25em',
}
const styleMonth = {
  width: '7.25em',
}
const styleDay = {
  width: '1.75em',
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

  onChange(field, value) {
    let stateClone = {...this.state}
    stateClone[field] = value

    this.setState({
      [field]: value,
      warning: '',
      startValueValid: this.isValidDate(
        stateClone.startDateYear,
        stateClone.startDateMonth,
        stateClone.startDateDay
      ),
      endValueValid: this.isValidDate(
        stateClone.endDateYear,
        stateClone.endDateMonth,
        stateClone.endDateDay
      ),
      dateRangeValid: this.isValidDateRange(
        this.textToNumeric(
          stateClone.startDateYear,
          stateClone.startDateMonth,
          stateClone.startDateDay
        ),
        this.textToNumeric(
          stateClone.endDateYear,
          stateClone.endDateMonth,
          stateClone.endDateDay
        )
      ),
    })
  }

  textToNumeric = (year, month, day) => {
    return {
      year: year ? _.toNumber(year) : null,
      month: month ? _.toNumber(month) : null,
      day: day ? _.toNumber(day) : null,
    }
  }

  isValidDate = (year, month, day) => {
    // No date given is technically valid (since a complete range is unnecessary)
    if (!year && !month && !day) {
      return true
    }

    // Valid date can be year only, year & month only, or full date
    if (year && !month && day) {
      // Year + day is not valid
      return false
    }

    let numeric = this.textToNumeric(year, month, day)

    const now = moment()
    const givenDate = moment(numeric)

    let validYear =
      _.isFinite(numeric.year) &&
      _.isInteger(numeric.year) &&
      numeric.year <= now.year()
    let validMonth = numeric.month
      ? numeric.month &&
        numeric.year &&
        moment([ numeric.year, numeric.month ]).isSameOrBefore(now)
      : true
    let validDay = numeric.day
      ? _.isFinite(numeric.day) &&
        _.isInteger(numeric.day) &&
        givenDate.isValid() &&
        givenDate.isSameOrBefore(now)
      : true

    return validYear && validMonth && validDay
  }

  isValidDateRange = (startMap, endMap) => {
    const now = moment()
    let startMoment = moment(startMap)
    let endMoment = moment(endMap)

    // No entered date will create a moment for now. Make sure if no data was entered, days are correctly identified as null
    let start =
      !startMap.year && startMoment.isSame(now, 'day') ? null : startMoment
    let end = !endMap.year && endMoment.isSame(now, 'day') ? null : endMoment

    // Valid date range can be just start, just end, or a start <= end
    if (start && end) {
      return start.isSameOrBefore(end)
    }
    else {
      return true
    }
  }

  clearDates = () => {
    this.props.removeDateRange()
    this.props.submit()

    this.setState(this.initialState())
  }

  applyDates = () => {
    if (!this.state.startValueValid || !this.state.endValueValid) {
      this.setState({
        warning: 'Invalid start and/or end date provided.',
      })
    }
    else if (!this.state.dateRangeValid) {
      this.setState({
        warning: 'Invalid date range provided.',
      })
    }
    else {
      let startDate = this.textToNumeric(
        this.state.startDateYear,
        this.state.startDateMonth,
        this.state.startDateDay
      )
      let endDate = this.textToNumeric(
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

  render() {
    const applyButton = (
      <Button
        key="TimeFilter::apply"
        text="Apply"
        onClick={this.applyDates}
        style={styleButton}
      />
    )

    const clearButton = (
      <Button
        key="TimeFilter::clear"
        text="Clear"
        onClick={this.clearDates}
        style={styleButton}
      />
    )

    return (
      <div style={styleTimeFilter}>
        <p>
          Provide a start date, end date, or date range. Use year, year and
          month, or full dates. Future dates are not accepted.
        </p>
        <form>
          <fieldset
            style={styleFieldset}
            onChange={event =>
              this.onChange(event.target.name, event.target.value)}
          >
            <legend>Start Date: </legend>
            <div style={styleLabels}>
              <label htmlFor="startDateYear">Year</label>
              <label htmlFor="startDateMonth">Month</label>
              <label htmlFor="startDateDay" style={{paddingRight: '0.5em'}}>
                Day
              </label>
            </div>
            <div style={styleInputs}>
              <input
                type="text"
                id="startDateYear"
                name="startDateYear"
                placeholder="YYYY"
                value={this.state.startDateYear}
                style={styleYear}
              />
              <select
                id="startDateMonth"
                name="startDateMonth"
                value={this.state.startDateMonth}
                style={styleMonth}
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
              <input
                type="text"
                id="startDateDay"
                name="startDateDay"
                placeholder="DD"
                value={this.state.startDateDay}
                style={styleDay}
              />
              <span
                aria-hidden="true"
                style={styleInputValidity(this.state.startValueValid)}
              >
                {this.state.startValueValid ? '✓' : '✖'}
              </span>
            </div>
          </fieldset>

          <fieldset
            style={styleFieldset}
            onChange={event =>
              this.onChange(event.target.name, event.target.value)}
          >
            <legend>End Date: </legend>
            <div style={styleLabels}>
              <label htmlFor="endDateYear">Year</label>
              <label htmlFor="endDateMonth">Month</label>
              <label htmlFor="endDateDay" style={{paddingRight: '0.5em'}}>
                Day
              </label>
            </div>
            <div style={styleInputs}>
              <input
                type="text"
                id="endDateYear"
                name="endDateYear"
                placeholder="YYYY"
                value={this.state.endDateYear}
                style={styleYear}
              />
              <select
                id="endDateMonth"
                name="endDateMonth"
                value={this.state.endDateMonth}
                style={styleMonth}
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
              <input
                type="text"
                id="endDateDay"
                name="endDateDay"
                placeholder="DD"
                value={this.state.endDateDay}
                style={styleDay}
              />
              <span
                aria-hidden="true"
                style={styleInputValidity(this.state.endValueValid)}
              >
                {this.state.endValueValid ? '✓' : '✖'}
              </span>
            </div>
          </fieldset>
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
