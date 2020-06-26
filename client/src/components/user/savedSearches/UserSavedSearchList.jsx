import React, { useState } from 'react'
import UserSavedSearch from './UserSavedSearch'
import ListView from '../../common/ui/ListView'

const savedSearches = {
  '1': {
    name: 'My Saved Search',
    url:
      'http://localhost:8888/onestop/collections?q=climate&g=47.5461,-26.1998,-109.6463,60.6288&gr=i',
  },
  '2': {
    name: 'Another search I saved',
    url:
      'http://localhost:8888/onestop/collections?q=climate&g=73.2733,65.5937,82.6364,71.5232&gr=i&tr=i&s=1999-01-01T00%3A00%3A00Z&e=2010-01-01T00%3A00%3A00Z',
  },
  '3': {
    name: 'My co-ops filter.',
    url:
      'http://localhost:8888/onestop/collections?q=co-ops&g=-175.9927,-15.7288,-32.0828,74.5945&gr=i&tr=d&s=1925-01-01T00%3A00%3A00Z&e=2010-04-05T00%3A00%3A00Z&f=science:Oceans%20%3E%20Ocean%20Temperature%20%3E%20Sea%20Surface%20Temperature;platforms:9751401%20-%20Ltbv3;dataCenters:DOC%2FNOAA%2FNESDIS%2FNODC%20%3E%20National%20Oceanographic%20Data%20Center%2C%20NESDIS%2C%20NOAA%2C%20U.S.%20Department%20of%20Commerce;dataFormats:NETCDF%20%3E%20NETCDF-4%20CLASSIC;projects:Physical%20Oceanographic%20Real-Time%20System%20(PORTS)',
  },
}

const UserSavedSearchList = ({ id }) => {
  const [offset, setOffset] = useState(0)
  const [currentPage, setCurrentPage] = useState(1)

  const propsForItem = (item, itemId) => {
    console.log('item', item)
    console.log('itemId', itemId)

    return { id: itemId, ...item }
  }

  return (
    <ListView
      totalRecords={savedSearches.length}
      items={savedSearches}
      ListItemComponent={UserSavedSearch}
      GridItemComponent={null}
      propsForItem={propsForItem}
      heading={'Saved Searches'}
      customActions={null}
      setOffset={(offset) => {
        setOffset(offset)
      }}
      currentPage={currentPage}
      setCurrentPage={(page) => setCurrentPage(page)}
    />
  )
}

export default UserSavedSearchList
