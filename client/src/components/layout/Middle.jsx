import React from 'react'
import Proxy from '../common/ui/Proxy'
import {MapProxyContext} from '../root/Root'

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
      <Proxy context={MapProxyContext} />
      {content}
    </main>
  )
  return contentElement
}

export default Middle
