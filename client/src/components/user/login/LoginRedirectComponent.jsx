import React, {useEffect} from 'react'
import {withRouter} from 'react-router'
import {getBasePath} from '../../../utils/urlUtils'

const LoginRedirectComponent = props => {
  const {loginEndpoint, configIsFetching} = props
  useEffect(() => {
    if (loginEndpoint != null) {
      window.location.href = loginEndpoint
    }
    else if (!configIsFetching && loginEndpoint == null) {
      window.location.href = getBasePath()
    }
  })
  return (
    <div>
      <h1>User session expired. Redirecting to the login screen...</h1>
    </div>
  )
}

export default withRouter(LoginRedirectComponent)
