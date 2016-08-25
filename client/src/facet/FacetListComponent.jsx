import React from 'react'
import styles from './facet.css'
import _ from 'lodash'
import Collapse, { Panel } from 'rc-collapse'

class FacetList extends React.Component {
  constructor(props) {
    super(props)
    console.log(props.categories)
    this.categories = props.categories
  }

  search(query) {
    this.updateQuery(query)
    this.submit(query)
  }

  render() {
    let facets = []
    let i = 0, j = 0
    _.forOwn(this.categories, function(v,k){
      facets.push(
        <Panel header={`${k}`} key={`${i++}`}>
          {v.map((obj)=> {
            return(<div>
              <Panel className={styles.subPanel} header={`${obj.term}`} key={`${j++}`}></Panel>
              <div className={`${styles.count} ${styles.numberCircle}`}>{`${obj.count}`}</div>
            </div>)
          })}
        </Panel>
      )
    })
    console.log(facets)

    return <div>
      <div className={`${styles.facetContainer}`}>
        <span className={'pure-menu-heading'}>Facets</span>
        <Collapse>
          {facets}
        </Collapse>
      </div>
    </div>
  }

}
export default FacetList
