import React from 'react'

import {consolidateStyles} from '../../../../utils/styleUtils'

const styleDay = {
  width: '1.5em',
  margin: 0,
  padding: '0 0.309em',
}

const DayField = props => {
  const {
    name,
    required,
    value,
    valid,
    onChange,
    styleLayout,
    styleLabel,
    styleLabelInvalid,
    styleRequiredIndicator,
    styleField,
    errorId,
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
        Day{required ? <span style={styleRequiredIndicator}>*</span> : null}
      </label>
      <input
        type="text"
        id={id}
        name={id}
        placeholder="DD"
        aria-placeholder="D D"
        value={value}
        aria-invalid={!valid}
        aria-required={required}
        aria-errormessage={errorId}
        onChange={onChange}
        maxLength="2"
        style={styleFieldApplied}
        aria-label={label}
      />
    </div>
  )
}
export default DayField
