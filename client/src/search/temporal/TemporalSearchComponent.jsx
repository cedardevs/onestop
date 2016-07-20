import React from 'react'
import {TransitionView, DateField, Calendar} from 'react-date-picker'
import { DateRange } from './TemporalActions'
import styles from './temporal.css'
import moment from 'moment'

const TemporalSearch = ({onChange, currentDate}) => {

  const formatAndEmit = (dateString, dateSelected) => {
    const value = dateString ? moment(dateString).format() : null
    onChange(value, dateSelected)
  }

  var startDate = [
    <DateField
        key='start'
        updateOnOk
        dateFormat="YYYY-MM-DD HH:mm"
        defaultValue=""
        onChange={(dateString)=>{formatAndEmit(dateString, DateRange.START_DATE)}}
    >
      <Calendar/>
    </DateField>
  ]

  var endDate = [
    <DateField
        key='end'
        updateOnOk
        dateFormat="YYYY-MM-DD HH:mm"
        defaultValue=""
        onChange={(dateString)=>{formatAndEmit(dateString, DateRange.END_DATE)}}
    >
      <Calendar/>
    </DateField>
  ]

  return (<div>
        <div className={styles.startTimeField} >
          {startDate}
        </div>
        <div className={styles.endTimeField}>
          {endDate}
        </div>
      </div>
  )
}

export default TemporalSearch
