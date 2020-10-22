import React, {useState} from 'react'
import UserSavedSearch from './UserSavedSearch'
import ListView from '../../common/ui/ListView'
import Meta from '../../helmet/Meta'
import {fontFamilySerif} from '../../../utils/styleUtils'
import _ from 'lodash'

// user.savedSearches = undefined;
const UserSavedSearchList = props => {
  const searches = props.savedSearches
  const [ offset, setOffset ] = useState(0)
  const [ currentPage, setCurrentPage ] = useState(1)
  const {navigateToSearch, deleteSearch} = props

  const propsForItem = (item, itemId) => {
    return {
      id: itemId,
      navigateToSearch: navigateToSearch,
      deleteSearch: deleteSearch,
      ...item,
    }
  }
  const styleCollections = {
    color: '#222',
  }

  const styleListHeading = {
    fontFamily: fontFamilySerif(),
    fontSize: '1.2em',
  }
  const heading = _.isEmpty(searches)
    ? 'You have no saved searches'
    : 'Saved Searches'
  const listHeading = (
    <h2 key="SavedSearch::listHeading" style={styleListHeading}>
      <span role="alert" aria-live="polite">
        {heading}
      </span>
    </h2>
  )

  return (
    <div style={styleCollections}>
      <Meta title={'User saved searches'} formatTitle={true} robots="noindex" />
      <ListView
        totalRecords={searches.length}
        items={searches}
        ListItemComponent={UserSavedSearch}
        GridItemComponent={null}
        propsForItem={propsForItem}
        heading={listHeading}
        customActions={null}
        setOffset={offset => {
          setOffset(offset)
        }}
        currentPage={currentPage}
        setCurrentPage={page => setCurrentPage(page)}
      />
    </div>
  )
}

export default UserSavedSearchList
