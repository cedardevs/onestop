import React from 'react'
import AppliedFilters from '../../filters/AppliedFilters'
import FlexRow from '../../common/ui/FlexRow'

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
  const styleQuery = {
    display: 'inline-flex',
    padding: '.25em .1em .25em .5em',
    margin: '1.618em',
    fontSize: '1.1em',
  }

  return (
    <FlexRow
      items={[
        <div style={styleQuery}>
          Query: {"'"}
          {collectionFilter.queryText}
          {"'"}
        </div>,
        <AppliedFilters
          {...collectionFilter}
          showAppliedFilters={true}
          excludeRemoveFilter={true}
        />,
      ]}
    />
  )
}

export default UserSavedSearchAppliedFilters
