import React from 'react'
import {TransitionView, DateField, Calendar } from 'react-date-picker'
import styles from './search.css'

const TemporalSearch = () => (
        <div>
          <div className={styles.startTimeField}>
            <DateField
                forceValidDate
                defaultValue={"2016-05-30 15:23:34"}
                dateFormat="YYYY-MM-DD HH:mm:ss">
              <TransitionView>
                <Calendar style={{padding: 10}}/>
              </TransitionView>
            </DateField>
          </div>
          <div className={styles.endTimeField}>
          <DateField
              forceValidDate
              defaultValue={"2016-05-30 15:23:34"}
              dateFormat="YYYY-MM-DD HH:mm:ss">
            <TransitionView>
              <Calendar style={{padding: 10}}/>
            </TransitionView>
          </DateField>
          </div>
        </div>
)



export default TemporalSearch


