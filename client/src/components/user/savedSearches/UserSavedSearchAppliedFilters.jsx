import React from 'react'
import AppliedFilters from '../../filters/AppliedFilters'

const UserSavedSearchAppliedFilters = ({ collectionFilter }) => {
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

  return <AppliedFilters {...collectionFilter} showAppliedFilters={true} />
}

export default UserSavedSearchAppliedFilters
