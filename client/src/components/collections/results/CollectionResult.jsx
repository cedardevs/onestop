import React, {Component} from 'react'

import CollectionAppliedFiltersContainer from '../filters/CollectionAppliedFiltersContainer'

const styleResult = {
  minHeight: '100vh',
  paddingTop: '1ex',
}

export default class CollectionResult extends Component {
  constructor(props) {
    super(props)
  }

  render() {
    return (
      <div style={styleResult}>
        <CollectionAppliedFiltersContainer />
        {this.props.children}
      </div>
    )
  }
}
