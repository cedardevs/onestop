import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import {FilterColors} from '../../../style/defaultStyles'

const styleInputHidden = {
  visibility: 'hidden',
  width: '1px',
  margin: 0,
  padding: 0,
}

// do not call this directly, it is expected to be managed by RadioButtonSet
const RadioButton = ({
  selected,
  disabled,
  id,
  name,
  label,
  value,
  setSelection,
  labelGetsFocus,
  styleLabel,
  styleInput,
  description,
  ariaExpanded,
}) => {
  const [ focusing, setFocusing ] = useState(false)
  const [ checked, setChecked ] = useState(selected) // selected is boolean

  useEffect(
    () => {
      setChecked(selected)
    },
    [ selected ]
  )

  const onClick = event => {
    if (disabled) {
      return
    }
    // prevent parent click from propagating (only fire onClick of checkbox (not parent component onClicks too)
    event.stopPropagation()
    setSelection(value)
  }

  const styleLabelApplied = {
    ...{display: 'inline-block'}, //, outline: 'none'},
    ...styleLabel,
    ...(labelGetsFocus && focusing
      ? {
          textDecoration: 'underline',
        }
      : {}),
  }

  // TODO labelGetsFocus overloaded to hide input TODO also figure out why this TODO isn't displaying correctly in my editor
  return (
    <div onClick={onClick} style={{display: 'flex'}}>
      <label
        htmlFor={id}
        style={styleLabelApplied}
        title={description}
        aria-label={`${description} ${label}`}
      >
        {label}
      </label>
      <input
        type="radio"
        id={id}
        name={name}
        value={value}
        style={styleInput}
        checked={checked}
        disabled={disabled}
        aria-expanded={ariaExpanded}
        onChange={() => {}}
        onFocus={() => setFocusing(true)}
        onBlur={() => setFocusing(false)}
      />
    </div>
  )
}
export default RadioButton
