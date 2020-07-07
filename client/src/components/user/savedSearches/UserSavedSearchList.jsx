import React, {useState} from 'react'
import UserSavedSearch from './UserSavedSearch'
import ListView from '../../common/ui/ListView'

function listToMap(searchList){
  let index = 1;
  let mapSearches = {}
  while (index <= searchList.length) {
    mapSearches[index.toString()] = searchList[index-1]
    index++;
  }
  return mapSearches
}
// user.savedSearches = undefined;
const UserSavedSearchList = props => {
  const searches = props.savedSearches ? listToMap(props.savedSearches ) : null
  const [ offset, setOffset ] = useState(0)
  const [ currentPage, setCurrentPage ] = useState(1)

  const propsForItem = (item, itemId) => {
    return {id: itemId, ...item}
  }

  return (
    <ListView
      totalRecords={searches.length}
      items={searches}
      ListItemComponent={UserSavedSearch}
      GridItemComponent={null}
      propsForItem={propsForItem}
      heading={'Saved Searches'}
      customActions={null}
      setOffset={offset => {
        setOffset(offset)
      }}
      currentPage={currentPage}
      setCurrentPage={page => setCurrentPage(page)}
    />
  )
}

export default UserSavedSearchList
