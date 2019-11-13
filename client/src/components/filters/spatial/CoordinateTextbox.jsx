import React, {useState} from 'react'
import {FilterColors} from '../../../style/defaultStyles'
import {fontFamilyMonospace} from '../../../utils/styleUtils'

const styleInputRow = {
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-between',
  margin: '0.618em 0',
  width: '15em',
}

const styleLabel = {
  width: '4em',
}

const styleCoordWrapper = {
  height: '2em',
}

const styleTextBox = {
  width: '10em',
  color: FilterColors.TEXT,
  fontFamily: fontFamilyMonospace(),

  height: '100%',
  margin: 0,
  padding: '0 0.309em',
  border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
  borderRadius: '0.309em',
}

const CoordinateTextbox = ({name, value, placeholder, onChange}) => {
  let id = `MapFilterCoordinatesInput::${name}`
  return (
    <div style={styleInputRow}>
      <label htmlFor={id} style={styleLabel}>
        {_.capitalize(name)}
      </label>
      <div style={styleCoordWrapper}>
        <input
          type="text"
          id={id}
          name={name}
          placeholder={placeholder}
          aria-placeholder={placeholder}
          value={value}
          style={styleTextBox}
          onChange={onChange}
        />
      </div>
    </div>
  )
}
export default CoordinateTextbox
