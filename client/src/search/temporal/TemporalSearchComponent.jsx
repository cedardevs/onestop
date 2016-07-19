import React from 'react'
import {TransitionView, DateField, Calendar} from 'react-date-picker'
import { DateRange } from './TemporalActions'
import styles from './temporal.css'
import moment from 'moment'

const TemporalSearch = ({onChange, currentDate}) => {

  const formatAndEmit = (dateString, dateSelected) => {
    onChange(moment(dateString).format(), dateSelected)
  }

  var startDate = [
    <DateField
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
