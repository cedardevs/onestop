import React from 'react'
import CSSModules from 'react-css-modules'
let styles = {}
import { buttons, forms, menus } from 'pure-css'
Object.assign(styles, buttons, forms)

const TextSearchField = ({onEnterKeyDown}) => {
  const handleSubmit = (e) => onEnterKeyDown(e.target.value);

  return <input
      hintText="Enter Search Term"
      fullWidth={true}
      onKeyDown={handleSubmit}
  />
}

export default TextSearchField
