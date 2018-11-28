import React from 'react'

class Login extends React.Component {
  constructor(props) {
    super(props)
    this.props = props
  }
  componentDidMount() {
    const {loginEndpoint} = this.props
    window.location.href = loginEndpoint
  }

  render() {
    return <div>"Please wait while we redirect you to the login page..."</div>
  }
}

export default Login
