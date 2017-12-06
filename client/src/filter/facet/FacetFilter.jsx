import React, { Component } from 'react'
import Expandable from '../../common/Expandable'
import FacetTree from './FacetTreeContainer'
import _ from 'lodash'

/**
  This component contains everything described by the facet map built from the facet results.
**/

const styleExpandableCategoryHeading = {
  backgroundColor: '#17478F',
  padding: '0.618em',
}

const styleExpandableWrapper = {
  marginBottom: '1px',
}

const styleExpandableCategoryContent = marginNest => {
  return {
    marginLeft: marginNest ? marginNest : '1em',
  }
}

export default class FacetFilter extends Component {
  constructor(props) {
    super(props)
    this.state = {
      openExpandables: {},
    }
  }

  handleExpandableToggle = (category, open) => {
    this.setState(prevState => {
      let openExpandables = Object.assign({}, prevState.openExpandables)
      openExpandables[category] = open
      return {
        ...prevState,
        openExpandables: openExpandables,
      }
    })
  }

  handleExpandToggleMouse = (event) => {
    this.handleExpandableToggle(event.value, event.open)
  }

  updateStoreAndSubmitSearch = (facet, select) => {
    const category = facet.category
    const term = facet.term
    const selected = select
    this.props.toggleFacet(category, term, selected)
    this.props.submit()
  }

  render() {
    let expandableCategories = []
    var self = this
    _.each( this.props.facetMap, function(value, category) {
      // show hamburger menu for high-level categories
      const expandableKey = `${category}`
      const headerId = `header-filter-${category.replace(' ','-','g')}`

      const highLevelHeading = <h3
        id={headerId}
        style={{fontSize: '1em', fontWeight: 'normal', display: 'inline', }}><span aria-hidden='true'>&#9776;&nbsp;</span>{category}</h3>

      const expandableFacets = <FacetTree
        headerId={headerId}
        facetMap={self.props.facetMap[category]}
        selectedFacets={self.props.selectedFacets}
        handleSelectToggle={self.updateStoreAndSubmitSearch}
        backgroundColor={self.props.backgroundColor}
        marginNest={self.props.marginNest}
        />

      // high-level categories (e.g. - "Data Themes" | "Platforms" | "Projects" | "Data Centers" | "Data Resolution")
      expandableCategories.push(
          <Expandable
              open={!!self.state.openExpandables[expandableKey]}
              key={expandableKey}
              value={expandableKey}
              heading={highLevelHeading}
              styleHeading={styleExpandableCategoryHeading}
              content={expandableFacets}
              styleWrapper={styleExpandableWrapper}
              styleContent={styleExpandableCategoryContent(self.props.marginNest)}
              onToggle={self.handleExpandToggleMouse}
          />,
      )
    })

    return <div>{expandableCategories}</div>
  }
}
