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
    const {disclaimer} = this.props

    if (!disclaimer) {
      return null
    }

    return <div style={styleDisclaimer}>{disclaimer}</div>
  }
}

export default Disclaimer
