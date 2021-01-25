import React from 'react'

import {consolidateStyles} from '../../../../utils/styleUtils'

const styleTime = {
  width: '5.236em',
  margin: 0,
  padding: '0 0.309em',
}

const TimeField = props => {
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
    ...styleTime,
    ...styleField,
  }

  const id = `${name}DateTime`
  const label = `time ${name}`

  return (
    <div style={styleLayout}>
      <label
        style={consolidateStyles(styleLabel, valid ? null : styleLabelInvalid)}
        htmlFor={id}
      >
        Time{required ? <span style={styleRequiredIndicator}>*</span> : null}
      </label>
      <input
        type="text"
        id={id}
        name={id}
        placeholder="HH:MM:SS"
        aria-placeholder="H H : M M : S S"
        value={value}
        aria-invalid={!valid}
        aria-required={required}
        aria-errormessage={errorId}
        onChange={onChange}
        maxLength="8"
        style={styleFieldApplied}
        aria-label={label}
      />
    </div>
  )
}
export default TimeField
