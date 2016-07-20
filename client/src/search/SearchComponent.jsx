import React from 'react'
import TemporalContainer from './temporal/TemporalContainer'
import TextSearchField from './TextSearchFieldComponent'
import styles from './search.css'
import 'purecss'

const SearchFacet = ({submit, updateQuery, searchText}) => {
  return <form className={styles['pure-form']}>
    <span className={styles.searchFields}>
      <div className={styles.textField}>
        <TextSearchField onEnterKeyDown={submit} onChange={updateQuery} value={searchText}/>
      </div>
      <div className={styles.dateTimeField}>
        <TemporalContainer />
      </div>
    </span>
  </form>
}

export default SearchFacet
