import React, {Component} from 'react'

import AppliedFiltersContainer from './AppliedFiltersContainer'

const styleResult = {
  paddingTop: '1.618em',
  minHeight: '100vh',
}

export default class Result extends Component {
  constructor(props) {
    super(props)
  }

  render() {
    return (
      <div style={styleResult}>
        <AppliedFiltersContainer/>
        {this.props.children}
      </div>
    )
  }
}
