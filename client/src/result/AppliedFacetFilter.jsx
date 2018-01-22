import React, {Component} from 'react'
import _ from 'lodash'
import AppliedFilterBubble from './AppliedFilterBubble'
import {titleCaseKeyword} from '../utils/keywordUtils'

const styleAppliedFacets = {
  display: 'flex',
  flexFlow: 'row wrap',
  padding: '0 2em 1em',
}

export default class AppliedFacetFilter extends Component {
  render() {
    const {selectedFacets, onUnselectFacet} = this.props

    let appliedFacets = []
    _.forEach(selectedFacets, (terms, category) => {
      _.forEach(terms, term => {
        const name = titleCaseKeyword(term) || 'DNE'
        const key = `appliedFilter::${term}`

        appliedFacets.push(
          <AppliedFilterBubble
            backgroundColor="#1F4B4D"
            borderColor="#237D81"
            text={name}
            key={key}
            onUnselect={() => onUnselectFacet(category, term)}
          />
        )
      })
    })

    if (appliedFacets.length > 0) {
      return <div style={styleAppliedFacets}>{appliedFacets}</div>
    }
    return null
  }
}
