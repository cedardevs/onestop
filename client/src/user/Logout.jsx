import React from 'react'

class Logout extends React.Component {
  constructor(props) {
    super(props)
    this.props = props
  }
  componentDidMount() {
    const {logoutUser, logoutEndpoint} = this.props
    logoutUser()
    window.location.href = logoutEndpoint
  }

  render() {
    return <div>"Please wait while we log you out.</div>
  }
}

export default Logout
