import React, {Component} from 'react'
import Expandable from '../../common/Expandable'
import FacetTree from './FacetTreeContainer'
import _ from 'lodash'
import {FilterTheme} from '../../common/defaultStyles'

/**
  This component contains everything described by the facet map built from the facet results.
**/

const styleFacetFilter = {
  backgroundColor: FilterTheme.MEDIUM,
}

const styleExpandableCategoryHeading = {
  backgroundColor: FilterTheme.DARK,
  color: FilterTheme.DARK_TEXT,
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

  handleExpandToggleMouse = event => {
    this.handleExpandableToggle(event.value, event.open)
  }

  updateStoreAndSubmitSearch = (facet, selected) => {
    const category = facet.category
    const term = facet.term
    this.props.toggleFacet(category, term, selected)
    this.props.submit()
  }

  render() {
    let expandableCategories = []
    _.each(this.props.facets, facetCategory => {
      // show hamburger menu for high-level categories
      const category = facetCategory.name
      const expandableKey = category

      const highLevelHeading = (
        <h3
          id={facetCategory.id}
          style={{fontSize: '1em', fontWeight: 'normal', display: 'inline'}}
        >
          <span aria-hidden="true">&#9776;&nbsp;</span>
          {category}
        </h3>
      )

      const expandableFacets = (
        <FacetTree
          headerId={facetCategory.id}
          facetMap={facetCategory.keywordFacets}
          hierarchy={facetCategory.hierarchy}
          handleSelectToggle={this.updateStoreAndSubmitSearch}
          backgroundColor={this.props.backgroundColor}
          marginNest={this.props.marginNest}
        />
      )

      // high-level categories (e.g. - "Data Themes" | "Platforms" | "Projects" | "Data Centers" | "Data Resolution")
      expandableCategories.push(
        <Expandable
          open={!!this.state.openExpandables[expandableKey]}
          key={expandableKey}
          value={expandableKey}
          heading={highLevelHeading}
          styleHeading={styleExpandableCategoryHeading}
          content={expandableFacets}
          styleWrapper={styleExpandableWrapper}
          styleContent={styleExpandableCategoryContent(this.props.marginNest)}
          onToggle={this.handleExpandToggleMouse}
        />
      )
    })
    return <div style={styleFacetFilter}>{expandableCategories}</div>
  }
}
