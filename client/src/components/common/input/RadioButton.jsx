import React, {useState, useEffect} from 'react'

import {consolidateStyles} from '../../../utils/styleUtils'

const styleContainerDefault = {display: 'flex'}

const styleLabelDefault = {display: 'inline-block'}

// do not call this directly, it is expected to be managed by RadioButtonSet
const RadioButton = ({
  selected,
  disabled,
  id,
  name,
  label,
  value,
  setSelection,
  ariaExpanded,
  styleContainer,
  styleLabel,
  styleLabelSelected,
  styleLabelFocused,
  styleInput,
}) => {
  const [ focusing, setFocusing ] = useState(false)
  const [ checked, setChecked ] = useState(selected)

  useEffect(
    // update internal checked value from props
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

  const styleLabelApplied = consolidateStyles(
    styleLabelDefault,
    styleLabel,
    focusing && styleLabelFocused ? styleLabelFocused : null,
    checked && styleLabelSelected ? styleLabelSelected : null
  )

  const styleContainerApplied = consolidateStyles(
    styleContainerDefault,
    styleContainer
  )

  return (
    <div onClick={onClick} style={styleContainerApplied}>
      <label htmlFor={id} style={styleLabelApplied}>
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
        onChange={() => {}}
        onFocus={() => setFocusing(true)}
        onBlur={() => setFocusing(false)}
      />
    </div>
  )
}
export default RadioButton
