import React from 'react'

const style = {
  flex: '0 0 auto',
  backgroundColor: '#222C37',
  zIndex: 2,
}

export default class Footer extends React.Component {
  render() {
    const {content} = this.props
    return (
      <footer role="contentinfo" style={style}>
        {content}
      </footer>
    )
  }
}
