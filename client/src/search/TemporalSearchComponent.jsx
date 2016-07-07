import React from 'react'
import { DateTimePicker } from'react-widgets/lib/DateTimePicker'
import { connect } from 'react-redux'
import styles from './search.css'
import 'purecss'


class TemporalSearch extends React.component {


  const dropDownEntries = function () {
    
   console.log('one stop')
  };

  render() {
    return (
        <div >
          <div>
            <button className={`${styles['pure-button']} ${styles.temporalDropdown}`} onClick={() => dropDownEntries }>
              Temporal
            </button>

          </div>
        </div>
    );
  }
}

export default TemporalSearch