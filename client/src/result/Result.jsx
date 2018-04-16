import React, {Component} from 'react'

import AppliedFiltersContainer from './AppliedFiltersContainer'

const styleResult = {
  minHeight: '100vh',
  paddingTop: '1ex',
}

export default class Result extends Component {
  constructor(props) {
    super(props)
  }

  render() {
    return (
      <div style={styleResult}>
        <AppliedFiltersContainer />
        {this.props.children}
      </div>
    )
  }
}
