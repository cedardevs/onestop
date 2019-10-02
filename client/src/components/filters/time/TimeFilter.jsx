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
import {
  FilterColors,
  FilterStyles,
  SiteColors,
} from '../../../style/defaultStyles'
import DateFieldset from './DateFieldset'

const styleTimeFilter = {
  ...FilterStyles.MEDIUM,
  ...{padding: '0.618em'},
}

const styleForm = {
  display: 'flex',
  flexDirection: 'column',
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
        // TODO this step can hopefully be moved into dateFieldset?
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
        // Note: this cannot tho (see above TODO)
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

  handleKeyDown = event => {
    if (event.keyCode === Key.ENTER) {
      event.preventDefault()
      this.applyDates()
    }
  }

  render() {
    const applyButton = this.createApplyButton()

    const clearButton = this.createClearButton()

    const onDateChange = event => {
      this.onChange(event.target.name, event.target.value)
    }

    const inputColumn = (
      <FlexColumn
        items={[
          <div key="DateFilterInput::all">
            <form
              style={styleForm}
              onKeyDown={this.handleKeyDown}
              aria-describedby="timeFilterInstructions"
            >
              <DateFieldset
                name="start"
                year={this.state.startDateYear}
                month={this.state.startDateMonth}
                day={this.state.startDateDay}
                valid={this.state.startValueValid}
                onDateChange={onDateChange}
              />
              <DateFieldset
                name="end"
                year={this.state.endDateYear}
                month={this.state.endDateMonth}
                day={this.state.endDateDay}
                valid={this.state.endValueValid}
                onDateChange={onDateChange}
              />
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
