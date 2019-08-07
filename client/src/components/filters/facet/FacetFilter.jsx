import React from 'react'
import Expandable from '../../common/ui/Expandable'
import FacetTree from './FacetTree'
import _ from 'lodash'
import {FilterStyles} from '../../../style/defaultStyles'

/**
  This component contains everything described by the facet map built from the facet results.
**/

const styleFacetFilter = FilterStyles.MEDIUM

const styleDescription = {
  margin: '0.618em',
}

const styleExpandableCategoryHeading = {
  ...FilterStyles.DARK,
  ...{padding: '0.618em'},
}

const styleExpandableWrapper = {
  marginBottom: '1px',
}

const styleExpandableCategoryContent = marginNest => {
  return {
    marginLeft: marginNest ? marginNest : '1em',
  }
}

export default class FacetFilter extends React.Component {
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
        <h4
          id={facetCategory.id}
          style={{fontSize: '1em', fontWeight: 'normal', display: 'inline'}}
        >
          <span aria-hidden="true">&#9776;&nbsp;</span>
          {category}
        </h4>
      )

      const expandableFacets = (
        <FacetTree
          headerId={facetCategory.id}
          facetMap={facetCategory.keywordFacets}
          hierarchy={facetCategory.hierarchy}
          handleSelectToggle={this.updateStoreAndSubmitSearch}
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

    return (
      <div style={styleFacetFilter}>
        <div style={styleDescription}>
          <legend id="facetFilterInstructions">
            In each subsection, you can use arrow keys navigate attributes, and
            space or enter to toggle selections.
          </legend>
        </div>
        {expandableCategories}
      </div>
    )
  }
}
