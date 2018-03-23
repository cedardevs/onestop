import React, {Component} from 'react'

const style = {
  flex: '0 0 auto',
  backgroundColor: '#222C37',
  zIndex: 2,
}

export default class Footer extends Component {
  render() {
    const {content} = this.props
    return <div style={style}>{content}</div>
  }
}
