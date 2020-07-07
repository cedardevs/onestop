import React from 'react'
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
  return (
    <div style={styleCenterContent}>
      <div style={styleDashboardWrapper}>
        <Meta title="User Dashboard for NOAA OneStop" />
        <section>
          <UserSavedSearchList
            user={props.user}
            savedSearches={props.savedSearches}
          />
        </section>
      </div>doc
    </div>
  )
}

export default UserDashboard
