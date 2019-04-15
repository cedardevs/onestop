import React from 'react'
import {SiteColors} from '../../style/defaultStyles'

const styleDisclaimer = {
  background: SiteColors.WARNING,
  textAlign: 'center',
  padding: '0.618em',
  fontSize: '1.2em',
}

class Disclaimer extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    const {message} = this.props

    if (!message) {
      return null
    }

    return <div style={styleDisclaimer}>{this.props.message}</div>
  }
}

export default Disclaimer
