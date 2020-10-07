import React from 'react'
import AppliedFilters from '../../filters/AppliedFilters'

const UserSavedSearchAppliedFilters = ({collectionFilter}) => {
  //   const {
  //     selectedFacets,
  //     startDateTime,
  //     endDateTime,
  //     startYear,
  //     endYear,
  //     bbox,
  //     excludeGlobal,
  //     geoRelationship,
  //     timeRelationship,
  //   } = collectionFilter

  return (<div>
    <div>Query: {collectionFilter.queryText}</div>
    <AppliedFilters {...collectionFilter} showAppliedFilters={true} />
  </div>)
}

export default UserSavedSearchAppliedFilters
