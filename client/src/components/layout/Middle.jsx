import React from 'react'
import Modal from '../common/ui/Modal'
import {MapModalContext} from '../root/Root'

const styleMiddle = () => {
  return {
    display: 'flex',
    flexDirection: 'column',
    overflowX: 'hidden',
    overflowY: 'auto',
    boxSizing: 'border-box',
    margin: '0 auto',
    width: '100%',
    outline: 'none',
  }
}

const Middle = props => {
  const {content} = props

  const contentElement = (
    <main id="mainBlock" tabIndex="-1" style={styleMiddle()}>
      <Modal context={MapModalContext} />
      {content}
    </main>
  )
  return contentElement
}

export default Middle
