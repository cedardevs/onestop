import React from 'react'
import {Modal} from '../common/ui/Modal'

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
  const {content, modal, modalOpen} = props

  const contentElement = (
    <main id="mainBlock" tabIndex="-1" style={styleMiddle()}>
      <Modal modal={modal} open={modalOpen} />
      {content}
    </main>
  )
  return contentElement
}

export default Middle
