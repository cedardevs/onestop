import React, {useEffect, useState} from 'react'
import Meta from '../../helmet/Meta'
import UserSavedSearchList from '../savedSearches/UserSavedSearchList'
import {boxShadow} from '../../../style/defaultStyles'
import LoginRedirectComponent from '../login/LoginRedirectComponent'
import PropTypes from 'prop-types'

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
  color: '#222',
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
  const [ savedSearchCount, setSavedSearchCount ] = useState(0)
  const {
    user,
    loginEndpoint,
    configIsFetching,
    navigateToSearch,
    deleteSearch,
    savedSearches,
    getSavedSearches,
  } = props

  useEffect(() => {
    // Update the saved searches when we navigate to this component
    //todo figure out if there is a better way to do this
    if (
      user.isAuthenticated &&
      !user.isFetching &&
      !user.isFetchingSearches &&
      savedSearches &&
      savedSearchCount < user.searches.length
    ) {
      getSavedSearches()
      setSavedSearchCount(user.searches.length)
    }
  })

  const dashboardElement = (
    <div style={styleDashboardWrapper}>
      <Meta title="User Dashboard for NOAA OneStop" />
      <section>
        <UserSavedSearchList
          navigateToSearch={filter => navigateToSearch(filter)}
          deleteSearch={id => deleteSearch(id)}
          user={user}
          savedSearches={savedSearches}
        />
      </section>
    </div>
  )

  const dashboardOrRedirect =
    user && user.isAuthenticated ? (
      dashboardElement
    ) : (
      <LoginRedirectComponent
        loginEndpoint={loginEndpoint}
        configIsFetching={configIsFetching}
        user={user}
      />
    )

  return <div style={styleCenterContent}>{dashboardOrRedirect}</div>
}

UserDashboard.propTypes = {
  user: PropTypes.object.isRequired,
  loginEndpoint: PropTypes.string,
  configIsFetching: PropTypes.bool.isRequired,
  savedSearches: PropTypes.array,
  getSavedSearches: PropTypes.func.isRequired,
  navigateToSearch: PropTypes.func.isRequired,
  deleteSearch: PropTypes.func.isRequired,
}

export default UserDashboard
