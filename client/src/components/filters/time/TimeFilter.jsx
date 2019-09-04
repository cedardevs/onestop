import React from 'react'
import moment from 'moment/moment'
import _ from 'lodash'
import FlexColumn from '../../common/ui/FlexColumn'
import Button from '../../common/input/Button'
import {Key} from '../../../utils/keyboardUtils'
import {
  ymdToDateMap,
  isValidDate,
  isValidDateRange,
} from '../../../utils/inputUtils'
import FilterFieldset from '../FilterFieldset'
import {
  FilterColors,
  FilterStyles,
  SiteColors,
} from '../../../style/defaultStyles'

const styleInputValidity = isValid => {
  return {
    paddingLeft: '5px',
    color: isValid ? SiteColors.VALID : SiteColors.WARNING,
  }
}

const styleTimeFilter = {
  ...FilterStyles.MEDIUM,
  ...{padding: '0.618em'},
}

const styleForm = {
  display: 'flex',
  flexDirection: 'column',
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
  width: '2.618em',
  color: FilterColors.TEXT,
  height: '100%',
  margin: 0,
  padding: '0 0.309em',
  border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
  borderRadius: '0.309em',
}

const styleMonth = {
  width: '7em',
  color: FilterColors.TEXT,
  height: '100%',
  margin: 0,
  padding: 0,
  border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
}
const styleDay = {
  width: '1.309em',
  color: FilterColors.TEXT,
  height: '100%',
  margin: 0,
  padding: '0 0.309em',
  border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
  borderRadius: '0.309em',
}

const styleButtonRow = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'center',
}

const styleButton = {
  width: '30.9%',
  padding: '0.309em',
  margin: '0 0.309em',
  fontSize: '1.05em',
}

export default class TimeFilter extends React.Component {
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
        color: SiteColors.WARNING,
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

  createYearField = (name, value, onChange) => {
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
            onChange={onChange}
            maxLength="4"
            style={styleYear}
            aria-label={label}
          />
        </div>
      </div>
    )
  }

  createMonthField = (name, value, onChange) => {
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
            onChange={onChange}
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

  createDayField = (name, value, onChange) => {
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
            onChange={onChange}
            maxLength="2"
            style={styleDay}
            aria-label={label}
          />
        </div>
      </div>
    )
  }

  createDateFieldset = (name, year, month, day, valid) => {
    const legendText = `${_.capitalize(name)} Date:`

    const onDateChange = event => {
      this.onChange(event.target.name, event.target.value)
    }

    return (
      <FilterFieldset legendText={legendText}>
        <div style={styleDate}>
          {this.createYearField(name, year, onDateChange)}
          {this.createMonthField(name, month, onDateChange)}
          {this.createDayField(name, day, onDateChange)}

          <div style={styleField}>
            <span />
            <span aria-hidden="true" style={styleInputValidity(valid)}>
              {valid ? '✓' : '✖'}
            </span>
          </div>
        </div>
      </FilterFieldset>
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

    const inputColumn = (
      <FlexColumn
        items={[
          <div key="DateFilterInput::all">
            <form
              style={styleForm}
              onKeyDown={this.handleKeyDown}
              aria-describedby="timeFilterInstructions"
            >
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
          </div>,
          <div key="DateFilter::InputColumn::Buttons" style={styleButtonRow}>
            {applyButton}
            {clearButton}
          </div>,
          <div
            key="DateFilter::InputColumn::Warning"
            style={this.warningStyle()}
            role="alert"
          >
            {this.state.warning}
          </div>,
        ]}
      />
    )

    return (
      <div style={styleTimeFilter}>
        <fieldset style={{padding: '0.618em'}}>
          <legend id="timeFilterInstructions">
            Provide a start date, end date, or both. Day and month are optional.
            Future dates are not accepted.
          </legend>
          {inputColumn}
        </fieldset>
      </div>
    )
  }
}
