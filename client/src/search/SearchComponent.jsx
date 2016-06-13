import React from 'react'
import IndexDropDown from './IndexDropDownComponent'
import TextSearchField from './TextSearchFieldComponent'
import styles from './search.css'

const handleSubmit = (e) => onEnterKeyDown(e.target.value)
const SearchFacet = ({indexName, submit, handleIndexChange}) => {
  return <form className={styles['pure-form']}>
    <fieldset>
      <div className={styles.textfield} id='search-text'>
        <TextSearchField onEnterKeyDown={submit}/>
      </div>
      <div>
        <IndexDropDown indexName={indexName} onChange={handleIndexChange}/>
      </div>
    </fieldset>
  </form>
}

export default SearchFacet
