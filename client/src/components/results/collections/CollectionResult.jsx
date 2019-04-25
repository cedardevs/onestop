import React from 'react'

import CollectionAppliedFiltersContainer from '../../filters/collections/CollectionAppliedFiltersContainer'

const styleResult = {
  minHeight: '100vh',
  paddingTop: '1ex',
}

export default class CollectionResult extends React.Component {
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
