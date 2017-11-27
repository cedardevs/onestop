import React, { Component } from 'react'
import Expandable from '../common/Expandable'
import Facet from './Facet'
import _ from 'lodash'

const styleExpandableCategoryHeading = {
  backgroundColor: '#17478F',
  padding: '0.618em',
}

const styleExpandableCategoryContent = marginNest => {
  return {
    marginLeft: marginNest ? marginNest : '1em',
    marginBottom: '1px'
  }
}

const styleExpandableHeading = backgroundColor => {
  return {
    padding: '0.618em',
    backgroundColor: backgroundColor ? backgroundColor : 'initial',
  }
}

const styleExpandableContent = marginNest => {
  return {
    marginLeft: marginNest ? marginNest : '1em',
  }
}

const styleLeafFacet = backgroundColor => {
  return {
    padding: '0.618em',
    backgroundColor: backgroundColor ? backgroundColor : 'initial',
  }
}

export default class FacetFilter extends Component {
  constructor(props) {
    super(props)
    this.facetMap = props.facetMap
    this.selectedFacets = props.selectedFacets
    this.toggleFacet = props.toggleFacet
    this.submit = props.submit
    this.updateStoreAndSubmitSearch = this.updateStoreAndSubmitSearch.bind(
        this,
    )
    this.state = {openExpandables: {}}
  }

  componentWillUpdate(nextProps) {
    this.facetMap = nextProps.facetMap
    this.selectedFacets = nextProps.selectedFacets
  }

  updateStoreAndSubmitSearch(e) {
    const category = e.value.category
    const term = e.value.term
    const selected = e.checked
    this.toggleFacet(category, term, selected)
    this.submit()
  }

  isSelected(category, term) {
    const selectedTerms = this.selectedFacets[category]
    if (!selectedTerms) {
      return false
    }
    else {
      return selectedTerms.includes(term)
    }
  }

  handleExpandableToggle = event => {
    this.setState(prevState => {
      let openExpandables = Object.assign({}, prevState.openExpandables)
      if (event.open) {
        openExpandables[event.value] = true
      } else {
        openExpandables[event.value] = false
      }

      return {
        ...prevState,
        openExpandables: openExpandables,
      }
    })
  }

  createFacetComponent = facet => {
    // handle any nulls that might get into this function
    let facetComponent = null
    if (!facet) {
      return facetComponent
    }

    // parent facets (has expandable sub-facets)
    if ('children' in facet && !_.isEmpty(facet.children)) {
      const expandableKey = `${facet.category}-${facet.term}`
      const facetKey = `facet-${facet.category}-${facet.term}`
      const facetId = _.concat(_.words(facet.category), _.words(facet.term)).join('-')

      facetComponent = (
          <Expandable
              open={!!this.state.openExpandables[expandableKey]}
              key={expandableKey}
              value={expandableKey}
              heading={
                <Facet
                    id={facetId}
                    selected={this.isSelected(facet.category, facet.term)}
                    key={facetKey}
                    term={facet.term}
                    category={facet.category}
                    count={facet.count}
                    onChange={this.updateStoreAndSubmitSearch}
                />
              }
              styleHeading={styleExpandableHeading(this.props.backgroundColor)}
              content={this.createFacetComponent(facet.children)}
              styleContent={styleExpandableContent(this.props.marginNest)}
              showArrow={true}
              onToggle={this.handleExpandableToggle}
          />
      )
    } else if ('children' in facet && _.isEmpty(facet.children)) {
      // leaf facet (contains no sub-layer facets)
      const leafFacetKey = `facet-${facet.category}-${facet.term}`
      const facetId = _.concat(_.words(facet.category), _.words(facet.term)).join('-')

      facetComponent = (
          <Facet
              id={facetId}
              selected={this.isSelected(facet.category, facet.term)}
              key={leafFacetKey}
              term={facet.term}
              category={facet.category}
              count={facet.count}
              style={styleLeafFacet(this.props.backgroundColor)}
              onChange={this.updateStoreAndSubmitSearch}
          />
      )
    } else {
      // for each key recurse
      let facetComponents = []
      Object.keys(facet).forEach(subFacet => {
        facetComponents.push(this.createFacetComponent(facet[subFacet]))
      })
      return facetComponents
    }
    return facetComponent
  }

  render() {
    let expandableCategories = []
    const categories = Object.keys(this.props.facetMap)

    categories.forEach(category => {
      // show hamburger menu for high-level categories
      const highLevelHeading = <span>&#9776;&nbsp;{category}</span>

      // do recursive magic for nested expandables
      const expandableFacets = this.createFacetComponent(
          this.props.facetMap[category],
      )

      const expandableKey = `${category}`

      // high-level categories (e.g. - "Data Themes" | "Platforms" | "Projects" | "Data Centers" | "Data Resolution")
      expandableCategories.push(
          <Expandable
              open={!!this.state.openExpandables[expandableKey]}
              key={expandableKey}
              value={expandableKey}
              heading={highLevelHeading}
              styleHeading={styleExpandableCategoryHeading}
              content={expandableFacets}
              styleContent={styleExpandableCategoryContent(this.props.marginNest)}
              onToggle={this.handleExpandableToggle}
          />,
      )
    })

    return <div>{expandableCategories}</div>
  }
}
