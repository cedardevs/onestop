import React from 'react'
import IndexDropDown from './IndexDropDownComponent'
import TemporalSearch from '../temporal/TemporalSearchComponent'
import TextSearchField from './TextSearchFieldComponent'
import styles from './search.css'
import 'purecss'

const SearchFacet = ({indexName, submit, handleIndexChange}) => {
  return <form className={styles['pure-form']}>
    <span className={styles.searchFields}>
      <div className={styles.textField}>
        <TextSearchField onEnterKeyDown={submit}/>
      </div>

      <div className={styles.dateTimeField}>
        <TemporalSearch onEnterKeyDown={submit}/>
      </div>

      <div className={styles.dropDown}>
        <IndexDropDown indexName={indexName} onChange={handleIndexChange}/>
      </div>
    </span>
  </form>
}

export default SearchFacet