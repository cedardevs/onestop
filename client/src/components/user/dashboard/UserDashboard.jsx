import React, {useEffect, useState} from 'react'
import {fontFamilySerif, fontFamilyMonospace} from '../../../utils/styleUtils'
import Meta from '../../helmet/Meta'
import UserSavedSearchList from '../savedSearches/UserSavedSearchList'
import {boxShadow} from '../../../style/defaultStyles'

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
}

const styleDashboardWrapper = {
  maxWidth: '80em',
  width: '80em',
  boxShadow: boxShadow,
  paddingTop: '1.618em',
  paddingBottom: '1.618em',
  backgroundColor: 'white',
  color: '#222',
}

const UserDashboard = props => {
  const [savedSearchCount, setSavedSearchCount] = useState(0);
  useEffect(() => {
    // Update the saved searches when we navigate to this component
    //todo figure out if there is a better way to do this
    if (!props.user.isFetchingSearches && props.user.searches && savedSearchCount < props.user.searches.length){
      props.getSavedSearches(props.savedSearchEndpoint)
      setSavedSearchCount(props.user.searches.length)
    }
  });

  const {navigateToSearch, deleteSearch} = props
  return (
    <div style={styleCenterContent}>
      <div style={styleDashboardWrapper}>
        <Meta title="User Dashboard for NOAA OneStop" />
        <section>
          <UserSavedSearchList
            navigateToSearch={filter => navigateToSearch(filter)}
            deleteSearch={id => deleteSearch( props.savedSearchEndpoint, id)}
            user={props.user}
            savedSearches={props.savedSearches}
          />
        </section>
      </div>
    </div>
  )
}

export default UserDashboard
