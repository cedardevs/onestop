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
  backgroundColor: '#5396CC',
  padding: '0.618em',
  color: '#F9F9F9',
}

const styleFieldset = {
  marginBottom: '1em',
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

const styleButtons = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-around',
}

const inputDateFormat = 'YYYY-MM-DD'
const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss'

export default class TimeFilter extends Component {

  constructor(props) {
    super(props)

    this.state = {
      startDateYear: '',
      startDateMonth: '',
      startDateDay: '',
      endDateYear: '',
      endDateMonth: '',
      endDateDay: '',
      startValueValid: true,
      endValueValid: true,
    }
  }

  onChange(field, value) {
    console.log(field, value)
    let stateClone = { ...this.state }
    stateClone[field] = value

    this.setState({
      [field]: value,
      startValueValid: this.isValidDate(stateClone.startDateYear, stateClone.startDateMonth, stateClone.startDateDay),
      endValueValid: this.isValidDate(stateClone.endDateYear, stateClone.endDateMonth, stateClone.endDateDay),
    })
  }

  isValidDate = (year, month, day) => {
    // Valid date can be year only, year & month only, or full date
    year = _.toNumber(year)
    month = month ? _.toNumber(month) : null
    day = _.toNumber(day)
    if (_.isNaN(day)) { day = null }

    const now = moment()
    const givenDate = moment([year, month, day])

    let validYear = _.isFinite(year) && _.isInteger(year) && year <= now.year()
    let validMonth = month ? month && year && moment([year, month]).isSameOrBefore(now) : true
    let validDay = day ? _.isFinite(day) && _.isInteger(day) && givenDate.isValid() && givenDate.isSameOrBefore(now) : true

    console.log('date validation: ', year, month, day, validYear, validMonth, validDay)

    return validYear && validMonth && validDay
  }

  isValidDateRange = (start, end) => {
    // Valid date range can be just start, just end, or a start <= end
    if (start && end) {
      return start.isSameOrBefore(end)
    }
    else { return true }
  }

  render() {
    const applyButton = (
      <Button
        key="TimeFilter::apply"
        text="Apply"
        onClick={() => console.log('apply dates')}
        style={{ width: '35%' }}
      />
    )

    const clearButton = (
      <Button
        key="TimeFilter::clear"
        text="Clear"
        onClick={() => console.log('clear dates')}
        style={{ width: '35%' }}
      />
    )

    return (
      <div style={styleTimeFilter}>
        <p>Filter your search results by providing a date range or just start/end date. (FIX)</p>
        <form>
          <fieldset style={styleFieldset} onChange={(event) => this.onChange(event.target.name, event.target.value)}>
            <legend>Start Date: </legend>
            <div style={styleLabels}>
              <label htmlFor='startDateYear'>Year</label>
              <label htmlFor='startDateMonth'>Month</label>
              <label htmlFor='startDateDay' style={{paddingRight: '0.5em'}}>Day</label>
            </div>
            <div style={styleInputs}>
              <input type='text' id='startDateYear' name='startDateYear' placeholder='YYYY' size='6'/>
              <select id='startDateMonth' name='startDateMonth' value={this.state.startDateMonth}>
                <option value=''>(none)</option>
                <option value='0'>January</option>
                <option value='1'>February</option>
                <option value='2'>March</option>
                <option value='3'>April</option>
                <option value='4'>May</option>
                <option value='5'>June</option>
                <option value='6'>July</option>
                <option value='7'>August</option>
                <option value='8'>September</option>
                <option value='9'>October</option>
                <option value='10'>November</option>
                <option value='11'>December</option>
              </select>
              <input type='text' id='startDateDay' name='startDateDay' placeholder='DD' size='3'/>
              <span aria-hidden='true' style={styleInputValidity(this.state.startValueValid)}>{this.state.startValueValid ? '✓' : '✖'}</span>
            </div>
          </fieldset>

          <fieldset style={styleFieldset} onChange={(event) => this.onChange(event.target.name, event.target.value)}>
            <legend>End Date: </legend>
            <div style={styleLabels}>
              <label htmlFor='endDateYear'>Year</label>
              <label htmlFor='endDateMonth'>Month</label>
              <label htmlFor='endDateDay' style={{paddingRight: '0.5em'}}>Day</label>
            </div>
            <div style={styleInputs}>
              <input type='text' id='endDateYear' name='endDateYear' placeholder='YYYY' size='6'/>
              <select id='endDateMonth' name='endDateMonth' value={this.state.endDateMonth}>
                <option value=''>(none)</option>
                <option value='0'>January</option>
                <option value='1'>February</option>
                <option value='2'>March</option>
                <option value='3'>April</option>
                <option value='4'>May</option>
                <option value='5'>June</option>
                <option value='6'>July</option>
                <option value='7'>August</option>
                <option value='8'>September</option>
                <option value='9'>October</option>
                <option value='10'>November</option>
                <option value='11'>December</option>
              </select>
              <input type='text' id='endDateDay' name='endDateDay' placeholder='DD' size='3'/>
              <span aria-hidden='true' style={styleInputValidity(this.state.endValueValid)}>{this.state.endValueValid ? '✓' : '✖'}</span>
            </div>
          </fieldset>
          <div style={styleButtons}>
            {clearButton}
            {applyButton}
          </div>
        </form>
      </div>
    )
  }
}
