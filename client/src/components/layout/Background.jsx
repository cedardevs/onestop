import React from 'react'

const pattern = require('../../../img/topography.png')

const styleBackgroundPattern = {
  background: `url(${pattern}) repeat`,
  backgroundSize: '50em',
}

const styleBackgroundGradient = {
  background:
    'linear-gradient(0deg, rgba(130, 186, 255, .5) 0%, rgba(225, 225, 235, .5) 100%)',
}

class Background extends React.Component {
  render() {
    return (
      <div style={styleBackgroundPattern}>
        <div style={styleBackgroundGradient}>{this.props.children}</div>
      </div>
    )
  }
}

export default Background
