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
    let i = 0
    _.forOwn(this.categories, function(v,k){
      facets.push(
        <Panel header={`${k}`} key={`${i++}`}>
        <p>Testing text</p>
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
