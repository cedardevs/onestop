import React from 'react'
import styles from './facet.css'
import _ from 'lodash'
import Collapse, { Panel } from 'rc-collapse'

class FacetList extends React.Component {
  constructor(props) {
    super(props)
    this.updateStoreAndSubmitSearch = this.updateStoreAndSubmitSearch.bind(this)
    this.categories = props.categories
    this.updateFacets = props.updateFacets
    this.submit = props.submit
  }

  componentWillUpdate(nextProps) {
    this.categories = nextProps.categories
  }

  updateStoreAndSubmitSearch(e) {
    // Update store's UI vals
    // Submit search
    const {name, value} = e.target.dataset
    const selected = e.target.checked
    const facet = {
      name,
      value,
      selected
    }
    this.updateFacets(facet)
    this.submit()
  }

  toTitleCase(str){
    return _.startCase(_.toLower((str.split(/(?=[A-Z])/).join(" "))))
  }

  subFacetLabel(str) {
    return str.split('>').pop().trim()
  }

  render() {
    let facets = []
    let self = this
    let i = 0, j = 0
    _.forOwn(this.categories, (terms,category) => {
      facets.push(
        <Panel header={`${self.toTitleCase(category)}`} key={`${i++}`}>
          {Object.keys(terms).map( term => {
            let input = {
              className: styles.checkFacet,
              'data-name': category,
              'data-value': term,
              id: `${category}-${term}`,
              type: 'checkbox',
              onChange: self.updateStoreAndSubmitSearch,
              checked: terms[term].selected
            }
            return(<div>
              <input {...input}/>
               <span className={styles.facetLabel}>{self.subFacetLabel(`${term}`)}</span>
              <div className={`${styles.count} ${styles.numberCircle}`}>{`(${terms[term].count})`}</div>
            </div>)
          })}
        </Panel>
      )
    })

    return <div>
      <div className={`${styles.facetContainer}`}>
        <form class="pure-form">
          <span className={'pure-menu-heading'}>Categories</span>
          <Collapse defaultActiveKey="0">
            {facets}
          </Collapse>
        </form>
      </div>
    </div>
  }

}
export default FacetList
