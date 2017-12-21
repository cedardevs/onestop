import React, {Component} from 'react'

const style = {
  flex: '0 0 auto',
  backgroundColor: '#222C37',
  zIndex: 2,
}

export default class Footer extends Component {
  render() {
    return <div style={style}>{this.props.content}</div>
  }
}
