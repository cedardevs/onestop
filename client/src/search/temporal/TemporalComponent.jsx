import React from 'react'
import { DateRange } from './TemporalActions'
import Calendar from 'rc-calendar'
import moment from 'moment'
import styles from './temporal.css'

class TemporalSearch extends React.Component {
  constructor(props) {
    super(props)
    this.startDateTime = props.startDateTime
    this.endDateTime = props.endDateTime
  }


  render() {
    return (
        <form className={`pure-form pure-form-aligned`}>
          <fieldset>
            <div className={`pure-control-group`}>
              <label htmlFor="startDate">Start Date</label>
              <input id="startDate" type="date"></input>
            </div>
            <div className={`pure-control-group`}>
              <label htmlFor="endDate">End Date</label>
              <input id="endDate" type="text" placeholder="YYYY-MM-DD"></input>
            </div>
          </fieldset>
        </form>
    )
  }
}

export default TemporalSearch