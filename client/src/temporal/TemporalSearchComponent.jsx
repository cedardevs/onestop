import React from 'react'
import {TransitionView, DateField, Calendar} from 'react-date-picker'
import styles from '../search/search.css'
import 'moment'

const TemporalSearch = ({onEnterKeyDown}) => {

  let startDateValue = Date.now();

  const onChange = (dateString, { dateMoment, timestamp }) => {
    console.log(dateString)
  }

  var startDate = [
    <DateField className = {styles['react-date-picker-theme-hackerone']}
        forceValidDate
        updateOnDateClick
        defaultValue={startDateValue}
        dateFormat="YYYY-MM-DD"
        onChange={(dateString, { dateMoment, timestamp}) => {}}
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
        updateOnDateClick
        defaultValue={startDateValue}
        dateFormat="YYYY-MM-DD"
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