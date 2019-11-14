import React from 'react'
import Drawer from './Drawer'

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

export default class Middle extends React.Component {
  render() {
    const {
      content,
      drawer,
      drawerOpen,
      onDrawerOpen,
      onDrawerClose,
    } = this.props
    const contentElement = (
      <main id="mainBlock" tabIndex="-1" style={styleMiddle()}>
        <Drawer
          content={drawer}
          open={drawerOpen}
          onOpen={onDrawerOpen}
          onClose={onDrawerClose}
        />
        {content}
      </main>
    )
    return contentElement
  }
}
