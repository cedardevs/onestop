import React from 'react'
import {SiteColors} from '../../style/defaultStyles'
import {isColor} from '../../utils/styleUtils'

const styleDisclaimer = (color, backgroundColor) => {
  return {
    color: isColor(color) ? color : SiteColors.HEADER_TEXT,
    background: isColor(backgroundColor) ? backgroundColor : SiteColors.WARNING,
    textAlign: 'center',
    padding: '0.618em',
    fontSize: '1.2em',
  }
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

    const {message, color, backgroundColor} = disclaimer

    return <div style={styleDisclaimer(color, backgroundColor)}>{message}</div>
  }
}

export default Disclaimer
