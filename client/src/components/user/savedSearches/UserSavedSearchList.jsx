import React, {useState} from 'react'
import UserSavedSearch from './UserSavedSearch'
import ListView from '../../common/ui/ListView'
import Meta from '../../helmet/Meta'
import {fontFamilySerif} from '../../../utils/styleUtils'

function listToMap(searchList){
  console.log('searchList')
  console.log(searchList)
  let index = 0
  let mapSearches = {}
  while (index < searchList.length) {
    let id = searchList[index].id
    mapSearches[id] = searchList[index]
    index++
  }
  return mapSearches
}
// user.savedSearches = undefined;
const UserSavedSearchList = props => {
  const searches = props.savedSearches ? listToMap(props.savedSearches) : null
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
  const heading =
    props.savedSearches && props.savedSearches.length > 0
      ? 'Saved Searches'
      : 'You have no saved searches'
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
