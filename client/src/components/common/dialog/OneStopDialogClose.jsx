import React from 'react'

export default () => {
  return (
    <svg
      width={'100%'}
      height={'100%'}
      viewBox={'0 0 1 1'}
      stroke={'black'}
      strokeWidth={'0.1'}
      style={{backgroundColor: 'transparent'}}
    >
      <path d="M 0 0 L 1 1" />
      <path d="M 0 1 L 1 0" />
    </svg>
  )
}
