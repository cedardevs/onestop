import React from 'react'
import { fontFamilySerif, fontFamilyMonospace } from '../../../utils/styleUtils'
import Meta from '../../helmet/Meta'
import UserSavedSearchList from '../savedSearches/UserSavedSearchList'
import { boxShadow } from '../../../style/defaultStyles'

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

const UserDashboard = () => {
  return (
    <div style={styleCenterContent}>
      <div style={styleDashboardWrapper}>
        <Meta title="User Dashboard for NOAA OneStop" />
        <section>
          <UserSavedSearchList />
        </section>
      </div>
    </div>
  )
}

export default UserDashboard
