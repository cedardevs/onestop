import React from 'react'
import FeaturedItems from './FeaturedItems'

class FeaturedDatasets extends React.Component {
  search = query => {
    const {submit, updateQuery} = this.props
    updateQuery(query)
    submit(query)
  }

  render() {
    const {featured} = this.props
    if (featured) {
      return (
        <div aria-labelledby="featuredDatasets">
          <h2 id="featuredDatasets">Featured Data Sets:</h2>
          <div>
            <FeaturedItems doSearch={this.search} items={featured} />
          </div>
        </div>
      )
    }
    else {
      return null
    }
  }
}

export default FeaturedDatasets
