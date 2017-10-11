import React from 'react'
import styles from './facet.css'
import _ from 'lodash'
import Collapse, { Panel } from 'rc-collapse'

class FacetList extends React.Component {
  constructor(props) {
    super(props)
    this.updateStoreAndSubmitSearch = this.updateStoreAndSubmitSearch.bind(this)
    this.toggleIsGlobalAndSubmit = this.toggleIsGlobalAndSubmit.bind(this)
    this.facetMap = props.facetMap
    //this.populateFacetComponent = this.populateFacetComponent.bind(this)
    this.populateAdditionalFacetsComponents  = this.populateAdditionalFacetsComponents.bind(this)
    //this.populateSubPanel = this.populateSubPanel.bind(this)
    this.selectedFacets = props.selectedFacets
    this.toggleFacet = props.toggleFacet
    this.toggleExcludeGlobal = props.toggleExcludeGlobal
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

  toggleIsGlobalAndSubmit(){
    this.toggleExcludeGlobal()
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
    let facets = []
    let self = this
    _.forOwn(this.state.allCategoryMap, (categoryMap, category) => {

      let rows = []
      _.forOwn(_.get(categoryMap, self[category], categoryMap), (value, name) => {
        let input = {
          className: styles.checkFacet,
          'data-name': category,
          'data-value': value.term,
          id: `${category}-${value.term}`,
          type: 'checkbox',
          onChange: self.updateStoreAndSubmitSearch,
          checked: self.isSelected(category, value.term)
        }
        rows.push(<div className={styles.shiftedContent} key={`${category}-${value.term}`}>
          <input {...input}/>
          <span className={styles.facetLabel} title={`${value.term}`}>{name}</span>
          <div className={`${styles.count} ${styles.numberCircle}`}>{`(${value.count})`}</div>
          {/*<div className={styles.button}>{self.renderDownButton(category, value.term, !_.isEmpty(value.children))}</div> /!*FIXME perform check ONCE here -- no div if no children*!/*/}
        </div>)
      })

      facets.push(
        <Panel header={`${self.state.terms[category.toLowerCase()] || self.toTitleCase(category)}`} key={`${category}`}>
          {rows}
        </Panel>
      )
    })

    return <div>
      <div className={`${styles.facetContainer}`}>
        <form className={`pure-form ${styles.formStyle}`}>
          <span className={'pure-menu-heading'}>Categories</span>
          <Collapse defaultActiveKey="0">
            {facets}
          </Collapse>
        </form>
      </div>
    </div>
  }

  populateAdditionalFacetsComponents(){
    const self = this
    return(
        <div key="excludeGlobal"  className={styles.facetItem}>
            <input type="checkbox" className={styles.additionalCheckFacet} checked={this.state.excludeGlobal}
                   onChange={self.toggleIsGlobalAndSubmit}/>
            <span className={styles.facetLabel}>  Exclude Global</span>
        </div>
    )
  }

}
export default FacetList
