import React from 'react'
import IndexDropDown from './IndexDropDownComponent'
import TextSearchField from './TextSearchFieldComponent'
import styles from './search.css'

const SearchFacet = ({submit}) => {
  return <form className={styles['pure-form']}>
    <span className={styles.searchFields}>
      <div className={styles.textField}>
        <TextSearchField onEnterKeyDown={submit}/>
      </div>
    </span>
  </form>
}

export default SearchFacet
