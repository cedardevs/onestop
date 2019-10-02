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
      start: {
        valid: true,
        date: {},
      },
      end: {valid: true, date: {}},
      dateRangeValid: true,
      warning: '',
    }
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

  onChange = (field, date, valid) => {
    let stateClone = {...this.state}
    stateClone[field] = {date: date, valid: valid}

    this.setState({
      [field]: {date: date, valid: valid},
      warning: '',
      dateRangeValid: isValidDateRange(
        stateClone.start.date,
        stateClone.end.date
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
    const {start, end, dateRangeValid} = this.state
    if (!start.valid || !end.valid || !dateRangeValid) {
      this.setState({
        warning: this.createWarning(start.valid, end.valid, dateRangeValid),
      })
    }
    else {
      let startDateString = !_.every(start.date, _.isNull)
        ? moment(start.date).utc().startOf('day').format()
        : null
      let endDateString = !_.every(end.date, _.isNull)
        ? moment(end.date).utc().startOf('day').format()
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
                date={this.props.startDateTime}
                onDateChange={this.onChange}
              />
              <DateFieldset
                name="end"
                date={this.props.endDateTime}
                onDateChange={this.onChange}
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
