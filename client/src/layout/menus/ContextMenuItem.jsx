import React, { Component } from 'react'

const styleMenuItemVisible = () => {
    return {
        width: '100%'
    }
}

const styleMenuItemHidden = () => {
    return {
        display: 'none'
    }
}

export default class ContextMenuItem extends Component {

    constructor(props) {
        super(props);
        this.state = { visible: true };
    }

    render() {
        const styleVisible = styleMenuItemVisible()
        const styleHidden = styleMenuItemHidden()
        const styles = Object.assign({}, this.state.visible ? styleVisible : styleHidden)

        return (
            <div style={styles}>
                { this.props.content }
            </div>
        )
    }
}