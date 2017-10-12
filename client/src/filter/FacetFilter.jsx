import React, { Component } from 'react';
import Expandable from './Expandable';
import _ from 'lodash';
import Checkbox from '../common/input/Checkbox'

export default class FacetFilter extends Component {
  constructor(props) {
    super(props)
    this.facetMap = props.facetMap
    this.selectedFacets = props.selectedFacets
    // this.toggleFacet = props.toggleFacet
    this.submit = props.submit
    this.updateStoreAndSubmitSearch = this.updateStoreAndSubmitSearch.bind(this)
  }

  componentWillUpdate(nextProps) {
    this.facetMap = nextProps.facetMap
    this.selectedFacets = nextProps.selectedFacets
  }

  updateStoreAndSubmitSearch(e) {
    console.log("updateStoreAndSubmitSearch::",this.props)
    const heading = e.value.heading
    const term = e.value.term
    const selected = e.checked

    toggleFacet(heading, term, selected)
    this.submit()
  }

	render() {
    let sections = []
    let isSubsection = true

    Object.keys(this.facetMap).forEach( heading => {
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
          content: <FacetFilter facetMap={content.children}/>,
          checkbox: <Checkbox value={{term: content.term, heading: heading}} onChange={this.updateStoreAndSubmitSearch} />
        })
      } else if ("children" in content && _.isEmpty(content.children)) {
        sections.push({
          count: content.count,
          term: content.term ? content.term : null,
          heading: heading,
          content: null,
          checkbox: <Checkbox value={{term: content.term, heading: heading}} onChange={this.updateStoreAndSubmitSearch} />
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
