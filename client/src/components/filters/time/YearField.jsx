import React from 'react'

const styleYear = {
  width: '2.618em',
  margin: 0,
  padding: '0 0.309em',
}

const YearField = props => {
  const {name, value, onChange, label, styleLayout, styleLabel, styleField, maxLength} = props
  const styleFieldApplied = {
    ...styleYear,
    ...styleField,
  }

  const id = `${name}DateYear`
  const ariaLabel = `year ${name}`
  const labelText = label ? label : 'Year'

  return (
    <div style={styleLayout}>
      <label style={styleLabel} htmlFor={id}>
        {labelText}
      </label>
      <input
        type="text"
        id={id}
        name={id}
        placeholder="YYYY"
        aria-placeholder="Y Y Y Y"
        value={value}
        onChange={onChange}
        maxLength={maxLength | 4}
        style={styleFieldApplied}
        aria-label={ariaLabel}
      />
    </div>
  )
}
export default YearField
