import React, { Component } from 'react'

const style = {
  display: 'flex',
  flexDirection: 'column',
}

export default class FlexColumn extends Component {
  render() {
    const styles = Object.assign({}, style, this.props.style)
    return (
        <div style={styles}>
          {this.props.items}
        </div>
    )
  }
}
