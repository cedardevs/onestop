import React from 'react'

import {consolidateStyles} from '../../../../utils/styleUtils'

const styleMonth = {
  width: '7em',
  margin: 0,
  padding: 0,
}

const MonthField = props => {
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
    ...styleMonth,
    ...styleField,
  }

  const id = `${name}DateMonth`
  const label = `month ${name}`

  return (
    <div style={styleLayout}>
      <label
        style={consolidateStyles(styleLabel, valid ? null : styleLabelInvalid)}
        htmlFor={id}
      >
        Month{required ? <span style={styleRequiredIndicator}>*</span> : null}
      </label>
      <select
        id={id}
        name={id}
        value={value}
        onChange={onChange}
        style={styleFieldApplied}
        aria-label={label}
        aria-invalid={!valid}
        aria-required={required}
        aria-errormessage={errorId}
      >
        <option value="">(none)</option>
        <option value="0">January</option>
        <option value="1">February</option>
        <option value="2">March</option>
        <option value="3">April</option>
        <option value="4">May</option>
        <option value="5">June</option>
        <option value="6">July</option>
        <option value="7">August</option>
        <option value="8">September</option>
        <option value="9">October</option>
        <option value="10">November</option>
        <option value="11">December</option>
      </select>
    </div>
  )
}
export default MonthField
