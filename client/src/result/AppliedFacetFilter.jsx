import React, {Component} from 'react'
import _ from 'lodash'
import AppliedFacet from './AppliedFacet'

const styleAppliedFacets = {
  display: 'flex',
  flexFlow: 'row wrap',
  padding: '0 2em 1.618em 2em',
}

export default class AppliedFacetFilter extends Component {
  render() {
    const {location, selectedFacets, onUnselectFacet} = this.props

    let appliedFacets = []
    if (!location.includes('files')) {
      _.forEach(selectedFacets, (terms, category) => {
        _.forEach(terms, term => {
          appliedFacets.push(
            <AppliedFacet
              key={term}
              category={category}
              term={term}
              onUnselect={() => onUnselectFacet(category, term)}
            />
          )
        })
      })
    }
    return <div style={styleAppliedFacets}>{appliedFacets}</div>
  }
}
