import React from 'react'
import { Link } from 'react-router'
import TextSearchField from './TextSearchFieldComponent'
import styles from './search.css'

const SearchFacet = ({submit, updateQuery, searchText}) => {
  return <form className={'pure-form'}>
    <span className={styles.searchFields}>
      <div className={styles.textField}>
        <TextSearchField onEnterKeyDown={submit} onChange={updateQuery} value={searchText}/>
      </div>
      <Link className={`pure-button ${styles.advancedButton}`} to="/">Advanced</Link>
    </span>
  </form>
}

export default SearchFacet
