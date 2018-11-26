import React from "react";


class Logout extends React.Component {
    constructor(props) {
        super(props)
        this.props = props
    }
    componentDidMount(){
        const {logoutUser} = this.props
        logoutUser()
        window.location.href = 'http://localhost:9393/auth/login-gov/logout';
    }

    render() {
        return <div>"Please wait while we log you out.</div>
    }
}

export default Logout;