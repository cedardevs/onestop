import React from 'react'

const styleYear = {
  width: '2.618em',
  // color: FilterColors.TEXT,
  // height: '100%',
  margin: 0,
  padding: '0 0.309em',
  // border: `1px solid ${FilterColors.LIGHT_SHADOW}`,
  // borderRadius: '0.309em',
}

const YearField = props => {
  // const {name, value, onChange, styleField, styleLabel, styleWrapper} = props
  const {name, value, onChange, styleLayout, styleLabel, styleWrapper, styleField} = props // TODO rename style names to styleWrapper, styleField instead of 'styleYear'
  const styleFieldApplied = {
    ...styleYear,
    ...styleField,
  }
  console.log('year styling', styleFieldApplied)
  const id = `${name}DateYear`
  const label = `year ${name}`

  return (
    <div style={styleLayout}>
      <label style={styleLabel} htmlFor={id}>
        Year
      </label>
      <div style={styleWrapper}>
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
    </div>
  )
}
export default YearField
