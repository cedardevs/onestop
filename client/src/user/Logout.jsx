import React from 'react'

class Logout extends React.Component {
  constructor(props) {
    super(props)
    // const {logoutUser, logoutEndpoint} = props
    // this.state = {
    //     logoutUser: logoutUser,
    //     logoutEndpoint: logoutEndpoint
    // }
  }
  // componentDidMount() {
  //   const {logoutUser, logoutEndpoint} = this.state
  //   logoutUser()
  //   window.location.href = logoutEndpoint
  // }

  render() {
    const {logoutUser} = this.props
    logoutUser()
    window.location.href = 'http://localhost:8097/onestop/api/logout'
    return <div>"Please wait while we log you out.</div>
  }
}

export default Logout
