import React, { Component } from 'react';
import PropTypes from 'prop-types';
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
    this.state = this.getDefaultState()
  }

  getDefaultState() {
    return {
      terms : {
        "science": "Data Theme"
      },
      allCategoryMap: {}
    }
  }

  componentWillUpdate(nextProps) {
    this.facetMap = nextProps.facetMap
    this.selectedFacets = nextProps.selectedFacets
  }

  componentWillReceiveProps(nextProps) {
    if(!_.isEqual(this.facetMap, nextProps.facetMap)) {

      const parsedMap = {}
      _.map(nextProps.facetMap, (terms, category) => {
        console.log(category)
        console.log(terms)
        if (!_.isEmpty(terms)) { // Don't load categories that have no results
          let categoryMap = {}

          if(category === 'science') {
            categoryMap = this.buildHierarchyMap(category, terms)
          }
          else {
            Object.keys(terms).map( term => {
              const name = term.split(' > ')
              categoryMap[name[0]] = {
                count: terms[term].count,
                children: {},
                parent: null,
                term: term
              }
            })
          }

          parsedMap[category] = categoryMap
        }
      })
      console.log(parsedMap)

      this.setState({
        allCategoryMap: parsedMap
      })
    }

  }

  toTitleCase(str){
    return _.startCase(_.toLower((str.split(/(?=[A-Z])/).join(" "))))
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

  buildHierarchyMap(category, terms) {
    console.log(category)
    console.log(terms)

    var createChildrenHierarchy = (map, hierarchy, term, value) => {
      const lastTerm = hierarchy.pop()
      if(!_.isEmpty(hierarchy)) {
        let i;
        for(i = 0; i < hierarchy.length; i++) {
          // Since hierarchical strings are received in alphabetical order, this traversal
          // down the nested object won't error out
          //_.defaults(map, map[hierarchy[i]].children)
          map = map[hierarchy[i]].children = map[hierarchy[i]].children || {}
        }
      }

      map = map[lastTerm] = value
      return map
    }

    let categoryMap = {}

    Object.keys(terms).map( term => {
      let hierarchy = term.split(' > ')
      const parentTerm = hierarchy[hierarchy.length - 2]
      const value = {
        count: terms[term].count,
        children: {},
        parent: parentTerm ? parentTerm : null,
        term: term
      }

      createChildrenHierarchy(categoryMap, hierarchy, term, value)
    })

    console.log(categoryMap)
    return categoryMap
  }


	render() {
    let self = this
    let sections = []
    let isSubsection = true

    Object.keys(categoryMap).forEach(heading => {
      const content = this.state.allCategoryMap[heading]
      if (!_.isObject(content)) {
        return
      }
      if ("children" in content && !_.isEmpty(content.children)) {
        // Facet with Children
        sections.push({
          count: content.count,
          term: content.term ? content.term : null,
          heading: heading,
          content: <FacetFilter facets={content.children}/>
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
        let headingHighLevel = self.state.terms[heading.toLowerCase()] || self.toTitleCase(heading)
        sections.push({
          count: null,
          term: content.term ? content.term : null,
          heading: headingHighLevel,
          content: <FacetFilter facets={content}/>
        })
      }
    })

    return <Expandable sections={sections} isSubsection={isSubsection}/>
	}
}
