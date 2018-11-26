import React from "react";


class Login extends React.Component {
    constructor(props) {
        super(props)
        this.props = props
    }
    componentDidMount(){
        window.location.href = 'http://localhost:9393/auth/login-gov/login/loa-1';
    }

    render() {
        return (<div>"Please wait while we redirect you to the login page..."</div>)
    }
}

export default Login;