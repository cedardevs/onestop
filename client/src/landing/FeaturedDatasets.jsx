import React from 'react'
import FeaturedItems from './FeaturedItems'

const styleFeaturedDatasetsLabel = {
  textAlign: 'center',
}

const styleFeaturedDatasets = {
  color: '#F9F9F9',
}

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
          <h2 style={styleFeaturedDatasetsLabel} id="featuredDatasets">
            Featured Data Sets
          </h2>
          <div style={styleFeaturedDatasets}>
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
