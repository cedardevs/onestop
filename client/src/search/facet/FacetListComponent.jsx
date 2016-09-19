import Immutable from 'immutable'
import React from 'react'
import styles from './facet.css'
import _ from 'lodash'
import Collapse, { Panel } from 'rc-collapse'

class FacetList extends React.Component {
  constructor(props) {
    super(props)
    this.updateStoreAndSubmitSearch = this.updateStoreAndSubmitSearch.bind(this)
    this.facetMap = props.facetMap
    this.selectedFacets = props.selectedFacets
    this.modifySelectedFacets = props.modifySelectedFacets
    this.submit = props.submit
  }

  componentWillUpdate(nextProps) {
    this.facetMap = nextProps.facetMap
    this.selectedFacets = nextProps.selectedFacets
  }

  updateStoreAndSubmitSearch(e) {
    const {name, value} = e.target.dataset
    const selected = e.target.checked

    if (selected){
      this.selectedFacets = this.selectedFacets.setIn([name,value,'selected'], selected)
    } else {
      this.selectedFacets = this.selectedFacets.deleteIn([name,value])
                                  .filter(x => x.size)
    }

    this.modifySelectedFacets(this.selectedFacets)
    this.submit(!!this.selectedFacets.size)
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
    _.forOwn(this.facetMap, (terms,category) => {
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
              defaultChecked: terms[term].selected
            }
            return(<div key={`${j++}`}>
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
        <form className="pure-form">
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
