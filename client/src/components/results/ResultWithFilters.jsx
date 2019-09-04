import React from 'react'

const styleResult = {
  minHeight: '100vh',
}

export default class ResultWithFilters extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    return <div style={styleResult}>{this.props.children}</div>
  }
}
