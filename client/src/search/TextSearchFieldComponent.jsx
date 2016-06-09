import React from 'react'
import CSSModules from 'react-css-modules'
import { buttons, forms, menus } from 'pure-css'
import search from './search.css'
let styles = {}
Object.assign(styles, buttons, forms, search)

const TextSearchField = ({onEnterKeyDown}) => {
  const handleSubmit = (e) => onEnterKeyDown(e.target.value);
  console.log("These are styles: " +  styles);

  return <input
      styleName="textField"
      hintText="Enter Search Term"
      fullWidth={true}
      onKeyDown={handleSubmit}
  />
}

export default TextSearchField
