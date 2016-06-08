import React from 'react'
import IndexDropDown from './IndexDropDownComponent'
import TextSearchField from './TextSearchFieldComponent'
//import styles from './search.css'
import CSSModules from 'react-css-modules'
let styles = {}
import { buttons, forms, menus } from 'pure-css'
Object.assign(styles, buttons, forms)

const handleSubmit = (e) => onEnterKeyDown(e.target.value)
const SearchFacet = ({indexName, submit, handleIndexChange}) => {
  return <form styleName='pure-form'>
    <fieldset>
      <div styleName='textfield' id='search-text'>
        <TextSearchField onEnterKeyDown={submit}/>
      </div>
      <div>
        <IndexDropDown indexName={indexName} onChange={handleIndexChange}/>
      </div>
    </fieldset>
  </form>
}

export default SearchFacet
