import React from 'react'

const styleYear = {
  width: '2.618em',
  margin: 0,
  padding: '0 0.309em',
}

const YearField = props => {
  const {name, value, onChange, styleLayout, styleLabel, styleField} = props
  const styleFieldApplied = {
    ...styleYear,
    ...styleField,
  }

  const id = `${name}DateYear`
  const label = `year ${name}`

  return (
    <div style={styleLayout}>
      <label style={styleLabel} htmlFor={id}>
        Year
      </label>
      <input
        type="text"
        id={id}
        name={id}
        placeholder="YYYY"
        aria-placeholder="Y Y Y Y"
        value={value}
        onChange={onChange}
        maxLength="4"
        style={styleFieldApplied}
        aria-label={label}
      />
    </div>
  )
}
export default YearField
