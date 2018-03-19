import React from 'react'

const pattern = require('../../img/topography.png')
const earth = require('../../img/Earth.jpg')

const styleBackgroundPattern = {
  background: `url(${pattern}) repeat`,
  backgroundSize: '50em',
}

const styleBackgroundGradient = {
  background:
    'linear-gradient(0deg, rgba(130, 186, 255, .5) 0%, rgba(225, 225, 235, .5) 100%)',
}

const styleBackgroundEarth = {
  background: `url(${earth})`,
  backgroundSize: 'cover',
}

const styleBackgroundEarthOverlay = {
  backgroundColor: 'rgba(60, 90, 139, .3)',
}

class Background extends React.Component {
  render() {
    const {onHomePage} = this.props
    const styleBackgroundImage = onHomePage
      ? styleBackgroundEarth
      : styleBackgroundPattern
    const styleBackgroundOverlay = onHomePage
      ? styleBackgroundEarthOverlay
      : styleBackgroundGradient
    return (
      <div style={styleBackgroundImage}>
        <div style={styleBackgroundOverlay}>{this.props.children}</div>
      </div>
    )
  }
}

export default Background
