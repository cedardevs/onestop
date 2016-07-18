import React from 'react'
import {TransitionView, DateField, Calendar} from 'react-date-picker'
import { DateRange } from './TemporalActions'
import styles from '../search.css'
import 'moment'

const TemporalSearch = ({onChange, currentDate}) => {

  let startDateValue = new Date();

  const formatAndEmit = (dateString, dateSelected) => {
    onChange(dateString + 'T00:00:00Z', dateSelected)
  }

  var startDate = [
    <DateField
        forceValidDate
        updateOnOk
        defaultValue={startDateValue}
        dateFormat="YYYY-MM-DD"
        onChange={(dateString)=>{formatAndEmit(dateString, DateRange.START_DATE)}}
    >
      <TransitionView>
        <Calendar
        />
      </TransitionView>
    </DateField>
  ];

  var endDate = [
    <DateField
        forceValidDate
        updateOnOk
        defaultValue={startDateValue}
        dateFormat="YYYY-MM-DD"
        onChange={(dateString)=>{formatAndEmit(dateString, DateRange.END_DATE)}}
    >
      <TransitionView>
        <Calendar
        />
      </TransitionView>
    </DateField>
  ];

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
