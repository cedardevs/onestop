import React from 'react'

const styleBanner = {
  background: 'red',
  textAlign: 'center',
  padding: '0.618em',
  fontSize: '1.2em'
}

class Banner extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    if (!this.props.message) {
      return null
    }

    const configStyle = {
      color: (this.props.colors && this.props.colors.text) || 'white',
      background: (this.props.colors && this.props.colors.background) || 'red',
    }

    return (
      <div style={configStyle} style={styleBanner}>
        {this.props.message}
      </div>
    )
  }
}

export default Banner
