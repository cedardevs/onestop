import React from 'react'
import moment from 'moment'

const YearMonthForm = ({ date, onChange }) => {
    const currentYear = (new Date()).getFullYear()
    const fromMonth = new Date(currentYear - 100, 0, 1, 0, 0)
    const toMonth = new Date()
    // Component will receive date, locale
    // function YearMonthForm({ date, onChange }) {
      const months = moment.months()

      const years = []
      for (let i = fromMonth.getFullYear(); i <= toMonth.getFullYear(); i++) {
        years.push(i)
      }

      const handleChange = function handleChange(e) {
        let { year, month } = e.target.form
        onChange(new Date(year.value, month.value))
        console.log("Year: " + year.value + ", Month: " + month.value)
      }

      return (
        <form className="DayPicker-Caption">
          <select name="month" onChange={ handleChange } value={ date.getMonth() }>
            { months.map((month, i) =>
              <option key={ i } value={ i }>
                { month }
              </option>)
            }
          </select>
          <select name="year" onChange={ handleChange } value={ date.getFullYear() }>
            { years.map((year, i) =>
              <option key={ i } value={ year }>
                { year }
              </option>)
            }
          </select>
        </form>
    )
}

export default YearMonthForm