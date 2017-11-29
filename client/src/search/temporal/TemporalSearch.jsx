import React from 'react'
import ReactDOM from 'react-dom'
import moment from 'moment'
import DateTimePicker from './DateTimePicker'
import styles from './temporal.css'


class TemporalSearch extends React.Component {
  constructor(props) {
    super(props)
    this.startDateTime = props.startDateTime
    this.endDateTime = props.endDateTime
    this.disabledEndDate = this.disabledEndDate.bind(this)
    this.disabledStartDate = this.disabledStartDate.bind(this)
    this.onChange = this.onChange.bind(this)
    this.updateState = this.updateState.bind(this)
    this.updateTemporalFilters = this.updateTemporalFilters.bind(this)
    this.state = this.initialState()
  }

  initialState() {
    return {
      startValue: null,
      endValue: null,
      startValueNode: null,
      endValueNode: null
    }
  }

  componentDidMount() {
    this.updateState(this)
    this.setState({
      startValueNode: ReactDOM.findDOMNode(this.startValue),
      endValueNode: ReactDOM.findDOMNode(this.endValue)
    })
  }

  componentWillReceiveProps(nextProps) {
    this.updateState(nextProps)
  }

  updateState(props) {
    this.setState({
      startValue: props.startDateTime ? moment(props.startDateTime) : null,
      endValue: props.endDateTime ? moment(props.endDateTime) : null
    })
  }

  disabledEndDate(endValue) {
    if (!endValue) {
      return false
    }
    const startValue = this.state.startValue
    if (!startValue) {
      return false
    }
    return endValue.isBefore(startValue)
  }

  disabledStartDate(startValue) {
    if (!startValue) {
      return false
    }
    const endValue = this.state.endValue
    if (!endValue) {
      return false
    }
    return endValue.isBefore(startValue)
  }

  onChange(field, value) {
    this.setState({
      [field]: value
    })
  }

  updateTemporalFilters() {
    const {startValue, endValue} = this.state
    let startString = startValue ? startValue.format() : ''
    let endString = endValue ? endValue.format() : ''
    this.props.updateOnChange(startString, endString)
    this.props.toggleSelf()
  }

  render() {
    return (
        <div className={styles.temporalContainer}>
          <div id='temporalContent' className={`pure-form pure-g ${styles.temporalContent}`}>
            <div className={`pure-u-1 ${styles.pickerLabel}`}>Start Date:</div>
            <div className={`pure-u-1 ${styles.pickerInput}`}>
              <DateTimePicker id="startValue"
                              value={this.state.startValue}
                              onChange={this.onChange.bind(this, 'startValue')}
                              disabledDate={this.disabledStartDate}
                              mountPoint={this.state.startValueNode}/>
            </div>
            <div className={`pure-u-1 ${styles.pickerLabel}`}>End Date:</div>
            <div className={`pure-u-1`} ref={endValue => this.endValue = endValue}>
              <DateTimePicker id="endValue"
                              value={this.state.endValue}
                              onChange={this.onChange.bind(this, 'endValue')}
                              disabledDate={this.disabledEndDate}
                              mountPoint={this.state.endValueNode}/>
            </div>
            <div className={`pure-u-1 ${styles.bottomButtonPanel}`}>
              <button className={`pure-button ${styles.cancelButton}`} onClick={this.props.toggleSelf}>Cancel</button>
              <button className={`pure-button ${styles.submitButton}`} onClick={this.updateTemporalFilters}>Apply To Search</button>
            </div>
          </div>
          <div ref={startValue => this.startValue = startValue}></div>
          <div ref={endValue => this.endValue = endValue}></div>
        </div>
    )
  }
}

export default TemporalSearch
