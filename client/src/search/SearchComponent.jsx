import React from 'react'
import TextSearchField from './TextSearchFieldComponent'
import styles from './search.css'

const SearchFacet = ({submit, searchText}) => {
  return <form className={styles['pure-form']}>
    <span className={styles.searchFields}>
      <div className={styles.textField}>
        <TextSearchField onEnterKeyDown={submit} value={searchText}/>
      </div>
    </span>
  </form>
}

export default SearchFacet
