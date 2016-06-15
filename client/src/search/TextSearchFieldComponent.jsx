import React from 'react'

const TextSearchField = ({onEnterKeyDown}) => {
  const handleKeyDown = (e) => {
    if (e.keyCode === 13) {
      e.preventDefault()
      onEnterKeyDown(e.target.value)
    }
  }

  return <input
    placeholder="Enter Search Term"
    onKeyDown={handleKeyDown}
  />
}

export default TextSearchField
