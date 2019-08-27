import React from 'react'
import {Link} from 'react-router-dom'

const styleGranuleListNavigation = {
  display: 'flex',
  justifyContent: 'center',
  margin: '1.618em',
  color: 'black',
}

export default class GranuleListNavigation extends React.Component {
  render() {
    const {collectionId, collectionTitle} = this.props

    // if we have the collection ID, let's go ahead and give
    // the user to easily link back to the collection details
    return collectionId ? (
      <div style={styleGranuleListNavigation}>
        <div>
          Return to&nbsp;
          <Link
            to={`/collections/details/${collectionId}`}
            title={collectionTitle ? collectionTitle : 'Return to collection.'}
          >
            collection
          </Link>.
        </div>
      </div>
    ) : null
  }
}
