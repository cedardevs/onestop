import React, {useState} from 'react'
import FlexRow from '../../common/ui/FlexRow'
import {FilterColors, SiteColors} from '../../../style/defaultStyles'
import {fontFamilyMonospace} from '../../../utils/styleUtils'

const styleInputRow = {
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

const styleInputValidity = isValid => {
  return {
    paddingLeft: '5px',
    width: '1em',
    color: isValid ? SiteColors.VALID : SiteColors.WARNING,
  }
}

const CoordinateTextbox = ({name, value, valid, placeholder, onChange}) => {
  let id = `MapFilterCoordinatesInput::${name}`
  return (
    <FlexRow
      style={styleInputRow}
      items={[
        <label key="label" htmlFor={id} style={styleLabel}>
          {_.capitalize(name)}
        </label>,
        <div key="input" style={styleCoordWrapper}>
          <input
            type="text"
            id={id}
            name={name}
            placeholder={placeholder}
            aria-placeholder={placeholder}
            aria-invalid={!valid}
            value={value}
            style={styleTextBox}
            onChange={onChange}
          />
        </div>,
        <span key="valid" aria-hidden="true" style={styleInputValidity(valid)}>
          {valid ? '✓' : '✖'}
        </span>,
      ]}
    />
  )
}
export default CoordinateTextbox
