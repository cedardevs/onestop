import React from 'react'

const styleResult508 = {
  paddingTop: '1.618em',
  overflowX: 'hidden',
  maxWidth: '40em',
  margin: '1em auto 8em',
  minHeight: '100vh',
}

class ResultLayout extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    return <div style={styleResult508}>{this.props.children}</div>
  }
}

export default ResultLayout
