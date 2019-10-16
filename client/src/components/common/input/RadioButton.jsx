import React, {useState, useEffect} from 'react'
import _ from 'lodash'
import {FilterColors} from '../../../style/defaultStyles'

const styleInputHidden = {
  visibility: 'hidden',
  width: '1px',
  margin: 0,
  padding: 0,
}

const styleRadioContainer = {
  width: '1em',
  height: '1em',
  minWidth: '1em',
  cursor: 'pointer',
  background: '#eee',
  border: '1px solid #333',
  borderRadius: '1em',
  display: 'flex',
  justifyContent: 'center',
  marginLeft: '0.309em',
}

const styleFocused = {
  boxShadow: '0 0 3pt 2pt #277CB2',
}

const styleDisabled = {
  // TODO
}

const styleRadiomark = {
  opacity: '0',
  width: '0.6em',
  height: '0.6em',
  background: FilterColors.DARK,
  borderRadius: '1em',
  alignSelf: 'center',
}

const styleRadioChecked = {
  opacity: '1',
}

const styleRadioHover = {
  opacity: '.8',
  boxShadow: `0 0 1pt 1pt ${FilterColors.DARK}`,
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
  focusing,
  setFocusing,
}) => {
  const [ hovering, setHovering ] = useState(false)
  const [ pressing, setPressing ] = useState(false)
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

  const onMouseOver = event => {
    if (disabled) {
      return
    }
    setHovering(true)
  }

  const onMouseOut = event => {
    if (disabled) {
      return
    }
    setHovering(false)
    setPressing(false)
  }

  const onMouseDown = event => {
    if (disabled) {
      return
    }
    setPressing(true)
  }

  const styleRadio = {
    ...styleRadioContainer,
    ...styleInput,
    ...(focusing && selected ? styleFocused : {}),
    ...(disabled ? styleDisabled : {}),
  }
  const styleMark = {
    ...styleRadiomark,
    ...(selected || (hovering && pressing) ? styleRadioChecked : {}),
    ...(hovering ? styleRadioHover : {}),
  }

  return (
    <div
      onClick={onClick}
      onMouseOver={onMouseOver}
      onMouseOut={onMouseOut}
      onMouseDown={onMouseDown}
    >
      <label
        htmlFor={id}
        style={{...{display: 'inline-block'}, ...styleLabel}}
        title={description}
        aria-label={`${description} ${label}`}
      >
        {label}
      </label>
      <div style={{display: 'inline-block'}}>
        <div
          role="radio"
          title={description}
          aria-checked={checked}
          aria-expanded={ariaExpanded}
          aria-label={label}
          aria-disabled={disabled}
          style={styleRadio}
        >
          <div style={styleMark} />
        </div>
      </div>
      <input
        type="radio"
        id={id}
        style={styleInputHidden}
        name={name}
        value={value}
        checked={checked}
        disabled={disabled}
        aria-expanded={ariaExpanded}
        onChange={() => {}}
      />
    </div>
  )
}
export default RadioButton
