import React from 'react'

import {consolidateStyles} from '../../../../utils/styleUtils'

const styleDay = {
  width: '1.309em',
  margin: 0,
  padding: '0 0.309em',
}

const DayField = props => {
  const {
    name,
    value,
    valid,
    onChange,
    styleLayout,
    styleLabel,
    styleLabelInvalid,
    styleField,
  } = props
  const styleFieldApplied = {
    ...styleDay,
    ...styleField,
  }

  const id = `${name}DateDay`
  const label = `day ${name}`

  return (
    <div style={styleLayout}>
      <label
        style={consolidateStyles(styleLabel, valid ? null : styleLabelInvalid)}
        htmlFor={id}
      >
        Day
      </label>
      <input
        type="text"
        id={id}
        name={id}
        placeholder="DD"
        aria-placeholder="D D"
        value={value}
        aria-invalid={!valid}
        onChange={onChange}
        maxLength="2"
        style={styleFieldApplied}
        aria-label={label}
      />
    </div>
  )
}
export default DayField
