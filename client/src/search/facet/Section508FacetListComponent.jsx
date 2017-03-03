import React from 'react'
import Immutable from 'seamless-immutable'
import styles from './facet.css'
import _ from 'lodash'
import Collapse, { Panel } from 'rc-collapse'

class FacetList extends React.Component {
  constructor(props) {
    super(props)
    this.updateStoreAndSubmitSearch = this.updateStoreAndSubmitSearch.bind(this)
    this.facetMap = props.facetMap
    this.populateFacetComponent = this.populateFacetComponent.bind(this)
    this.populateSubPanel = this.populateSubPanel.bind(this)
    this.selectedFacets = props.selectedFacets
    this.toggleFacet = props.toggleFacet
    this.submit = props.submit
    this.state = this.getDefaultState()
  }

  getDefaultState() {
    return {
      terms : {
        "science": "Data Theme"
      }
    }
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

  populateFacetComponent() {
    const self = this
    const toTitleCase = str => _.startCase(_.toLower((str.split(/(?=[A-Z])/).join(" "))))
    let facets = []
    _.forOwn(this.facetMap, (terms,category) => {
      if (!_.isEmpty(terms)) { // Don't load categories that have no results
        facets.push(
          <Panel header={`${this.state.terms[category.toLowerCase()] ||
            toTitleCase(category)}`} key={`${category}`}>
            {self.populateSubPanel(category, terms)}
          </Panel>
        )
      }
    })
    return facets
  }

  populateSubPanel(category, subCategories) {
    const self = this
    const subFacetLabel = str => str.split('>').pop().trim()

    return Object.keys(subCategories).sort((a, b) => {
      const aSub = subFacetLabel(a)
      const bSub = subFacetLabel(b)
      if(aSub < bSub) { return -1 }
      if(aSub > bSub) { return 1 }
      return 0
    }).map( subCategory => {
      let input = {
        className: styles.checkFacet,
        'data-name': category,
        'data-value': subCategory,
        id: `${category}-${subCategory}`,
        type: 'checkbox',
        onChange: self.updateStoreAndSubmitSearch,
        checked: self.isSelected(category, subCategory)
      }
      return(<div key={`${category}-${subCategory}`}>
        <input {...input}/>
        <span className={styles.facetLabel}>{subFacetLabel(`${subCategory}`)}</span>
        <div className={`${styles.count} ${styles.numberCircle}`}>
          {`(${subCategories[subCategory].count})`}</div>
      </div>)
    })
  }

  render() {
    return <div>
      <div className={`${styles.facetContainer}`}>
        <form className={`pure-form ${styles.formStyle}`}>
          <span className={'pure-menu-heading'}>Categories</span>
          <Collapse defaultActiveKey="0">
            {this.populateFacetComponent()}
          </Collapse>
        </form>
      </div>
    </div>
  }

}
export default FacetList
