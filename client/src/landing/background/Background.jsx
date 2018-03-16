import React from 'react'
import styles from './background.css'
import Modernizr from 'modernizr'

const pattern = require('../../../img/topography.png')

const styleBackgroundGradient = {
  position: 'absolute',
  top: 0,
  left: 0,
  bottom: 0,
  right: 0,
  zIndex: -5000,
  minHeight: '100vh',
  minWidth: '100vw',
  background: '#4261cf',
  background: 'linear-gradient(0deg, #023E89 0%, #333 100%)',
}

const styleBackground = {
  position: 'absolute',
  top: 0,
  left: 0,
  bottom: 0,
  right: 0,
  background: `url(${pattern}) repeat`,
  backgroundSize: '50em',
  opacity: '0.5',
  width: '100%',
  minHeight: '100vh',
  zIndex: -4999,
}

class Background extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    var backgroundStyle = this.props.showImage
      ? styles.backgroundImage
      : styles.backgroundSolid
    var backgroundOverlay = this.props.showOverlay
      ? styles.backgroundOverlay
      : {}

    return (
      <div style={styleBackgroundGradient}>
        <div style={styleBackground} />
      </div>
    )
  }
}

export default Background
