import React from 'react'
import styles from './facet.css'
import _ from 'lodash'
import Collapse, { Panel } from 'rc-collapse'

class FacetList extends React.Component {
  constructor(props) {
    super(props)
    this.categories = props.categories
  }

  updateAndSubmitSearch(e) {
    const {name, value} = e.target.dataset
    const selected = e.target.checked
    // Update query
    // Submit query
  }

  render() {
    let facets = []
    let self = this
    let i = 0, j = 0
    _.forOwn(this.categories, function(v,k){
      facets.push(
        <Panel header={`${k}`} key={`${i++}`}>
          {v.map((obj)=> {
            return(<div>
              <input className={styles.checkFacet} data-name={`${k}`} data-value={`${obj.term}`} id={`${k}-${obj.term}`} type="checkbox"
               onChange={self.updateAndSubmitSearch}/><span className={styles.facetLabel}>{`${obj.term}`}</span>
              <div className={`${styles.count} ${styles.numberCircle}`}>{`(${obj.count})`}</div>
            </div>)
          })}
        </Panel>
      )
    })

    return <div>
      <div className={`${styles.facetContainer}`}>
        <form class="pure-form">
          <span className={'pure-menu-heading'}>Facets</span>
          <Collapse>
            {facets}
          </Collapse>
        </form>
      </div>
    </div>
  }

}
export default FacetList
