import React, {Component} from 'react'
import {buildGovExternalOnClick} from '../../utils/urlUtils'

const stylePointer = {
  cursor: 'pointer',
}

class Link extends Component {
  constructor(props) {
    super(props)
  }

  render() {
    const {href, target, onClick, ...others} = this.props
    return (
      <a
        href={href}
        target={target}
        onClick={buildGovExternalOnClick(href, target, onClick)}
        style={stylePointer}
        {...others}
      >
        {this.props.children}
      </a>
    )
  }
}

export default Link
