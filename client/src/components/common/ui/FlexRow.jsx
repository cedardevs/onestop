import React from 'react'

const style = {
  display: 'flex',
  flexDirection: 'row',
}

export default class FlexRow extends React.Component {
  render() {
    const {rowId, tabIndex, role} = this.props
    const styles = Object.assign({}, style, this.props.style)
    return (
      <div id={rowId} tabIndex={tabIndex} role={role} style={styles}>
        {this.props.items}
      </div>
    )
  }
}
