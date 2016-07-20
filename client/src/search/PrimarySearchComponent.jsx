import React from 'react'
import styles from './primarySearch.css'

const PrimarySearchComponent = ({onEnterKeyDown, onChange}) => {
  const handleKeyDown = (e) => {
    if (e.keyCode === 13) {
      e.preventDefault()
      onEnterKeyDown(e.target.value)
    }
  }

  const handleChange = (e) => {
    e.preventDefault()
    onChange(e.target.value)
  }

  return <input
      className={styles.textField}
      placeholder="Enter Search Term"
      onKeyDown={handleKeyDown}
      onChange={handleChange}
    />
}

export default PrimarySearchComponent
