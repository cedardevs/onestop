import React from 'react'
import ReactDOM from 'react-dom'
import './link.css'

class LinkComponent extends React.Component {
  constructor(props) {
    super(props)
  }

  generateLink() {
    const leavingSiteMsg = `You are exiting an NCEI website.
\nThank you for visiting our site. We have provided\
 a link because it has information that may interest you. NCEI does not\
 endorse the views expressed, the information presented, or any commercial\
 products that may be advertised or available on that site`

    const { host } = location
    let { href } = this.props
    if (href && href.split('/', 3).includes(host)) {
      return <a></a>
    } else {
      let {href, ...aProps} = this.props
      return <a {...aProps}
        onClick={()=> {
          if (window.confirm(leavingSiteMsg)) {
            window.location.href = href
          }
        }}></a>
    }
  }

  render() {
    return this.generateLink()
  }
}

export default LinkComponent
