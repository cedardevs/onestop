import React from 'react'
import {isGovExternal} from '../../utils/urlUtils'
import './link.css'

class Link extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    const { href, target, onClick, ...others } = this.props
    return <a
        href={href}
        target={target}
        onClick={this.buildOnClick(href, target, onClick)}
        {...others}>
      {this.props.children}
    </a>
  }

  buildOnClick(href, target, onClick) {
    const leavingSiteMsg = `The site you are navigating to is not hosted by the US Government.

Thank you for visiting our site. We have provided \
this link because it has information that may interest you, but we do not \
endorse the views expressed, the information presented, or any commercial \
products that may be advertised or available on that site.`

    return (e) => {
      if (typeof onClick === 'function') {
        onClick()
      }

      if (isGovExternal(href)) {
        e.preventDefault()
        if (window.confirm(leavingSiteMsg)) {
          target ? window.open(href, target) : window.location.href = href
        }
      }
    }
  }
}

export default Link
