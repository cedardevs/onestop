import React from 'react'

const styleDay = {
  width: '1.309em',
  margin: 0,
  padding: '0 0.309em',
}
// createDayField = (name, value, onChange) => {
//   const id = `${name}DateDay`
//   const label = `day ${name}`
//   return (
//     <div style={styleField}>
//       <label style={styleLabel} htmlFor={id}>
//         Day
//       </label>
//       <div style={styleDayWrapper}>
//         <input
//           type="text"
//           id={id}
//           name={id}
//           placeholder="DD"
//           aria-placeholder="D D"
//           value={value}
//           onChange={onChange}
//           maxLength="2"
//           style={styleDay}
//           aria-label={label}
//         />
//       </div>
//     </div>
//   )
// }
const DayField = props => {
  // const {name, value, onChange, styleField, styleLabel, styleWrapper} = props
  const {name, value, onChange, styleLayout, styleLabel, styleWrapper, styleField} = props // TODO rename style names to styleWrapper, styleField instead of 'styleYear'
  const styleFieldApplied = {
    ...styleDay,
    ...styleField,
  }
  console.log('day styling', styleFieldApplied)
  const id = `${name}DateDay`
  const label = `day ${name}`
  return (
    <div style={styleLayout}>
      <label style={styleLabel} htmlFor={id}>
        Day
      </label>
      <div style={styleWrapper}>
        <input
          type="text"
          id={id}
          name={id}
          placeholder="DD"
          aria-placeholder="D D"
          value={value}
          onChange={onChange}
          maxLength="2"
          style={styleFieldApplied}
          aria-label={label}
        />
      </div>
    </div>
  )
}
export default DayField
