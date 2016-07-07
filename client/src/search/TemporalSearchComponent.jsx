import React from 'react'
import { DateTimePicker } from'react-widgets/lib/DateTimePicker'
import { connect } from 'react-redux'
import styles from './search.css'
import 'purecss'


const TemporalSearch = () => {

  const dropDownEntries = function() {
   alert('one stop')
  }

  return (
    <button className={`${styles['pure-button']} ${styles.temporalDropdown}`} onClick={dropDownEntries}>
      Temporal
    </button>
  )
}

export default TemporalSearch
