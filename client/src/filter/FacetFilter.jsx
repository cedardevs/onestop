import React, { Component } from 'react';
import Expandable from './Expandable';
import _ from 'lodash';

export default class FacetFilter extends Component {
  constructor(props) {
    super(props)
    this.updateStoreAndSubmitSearch = this.updateStoreAndSubmitSearch.bind(this)
    this.facetMap = props.facetMap
    this.selectedFacets = props.selectedFacets
    this.toggleFacet = props.toggleFacet
    this.submit = props.submit
  }

  componentWillUpdate(nextProps) {
    this.facetMap = nextProps.facetMap
    this.selectedFacets = nextProps.selectedFacets
  }

  updateStoreAndSubmitSearch(e) {
    const {name, value} = e.target.dataset
    const selected = e.target.checked

    this.toggleFacet(name, value, selected)
    this.submit()
  }

  isSelected(category, facet) {
    return this.selectedFacets[category]
      && this.selectedFacets[category].includes(facet)
      || false
  }

	render() {
    let sections = []
    let isSubsection = true

    Object.keys(this.facetMap).forEach( heading => {
      console.log(heading)
      const content = this.facetMap[heading]
      if (!_.isObject(content)) {
        return
      }
      if ("children" in content && !_.isEmpty(content.children)) {
        // Facet with Children
        sections.push({
          count: content.count,
          term: content.term ? content.term : null,
          heading: heading,
          content: <FacetFilter facetMap={content.children}/>
        })
      } else if ("children" in content && _.isEmpty(content.children)) {
        sections.push({
          count: content.count,
          term: content.term ? content.term : null,
          heading: heading,
          content: null
        })
      } else {
        // High-Level Facet Section
        isSubsection = false;
        sections.push({
          count: null,
          term: content.term ? content.term : null,
          heading: heading,
          content: <FacetFilter facetMap={content}/>
        })
      }
    })

    return <Expandable sections={sections} isSubsection={isSubsection}/>
	}
}
