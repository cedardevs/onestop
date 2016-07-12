import React from 'react'
import {TransitionView, DateField, Calendar} from 'react-date-picker'
import styles from './search.css'


const TemporalSearch = () => {

  let startDateValue = Date.now();

  var startDate = [
    <DateField className = {styles['react-date-picker-theme-hackerone']}
        forceValidDate
        defaultValue={startDateValue}
        dateFormat="YYYY-MM-DD HH:mm:ss">
      <TransitionView>
        <Calendar style={{size: 10}} />
      </TransitionView>
    </DateField>
  ];

  var endDate = [
    <DateField
        forceValidDate
        defaultValue={startDateValue}
        dateFormat="YYYY-MM-DD HH:mm:ss">
      <TransitionView>
        <Calendar  className={styles.hackerone}/>
      </TransitionView>
    </DateField>
  ];

  var changeDate = [
    <div className={styles.dateTimeField}>
      <button className={`${styles['pure-button']} ${styles.temporalDropdown}`}>
        Temporal
      </button>
    </div>
  ]

  return ( <div>
        <div className={styles.startTimeField}>
          {startDate}
        </div>
        <div className={styles.endTimeField}>
          {endDate}
        </div>

        { changeDate }
      </div>
  )
}

export default TemporalSearch