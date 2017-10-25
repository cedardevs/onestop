import React, { Component } from 'react'

const styleTabButton = (active) => {
    return {
        display: 'flex',
        flexGrow: 1,
        borderLeft: '1px solid white',
        color: active ? '#212121' : '#FEFEFE',
        backgroundColor: active ? '#2de07a' : '#196aa8'
    }
}

const styleTabButtonInput = () => {
    return {
        display: 'none'
    }
}

const styleTabButtonLabel = (active, padding) => {
    return {
        width: '100%',
        padding: padding,
        fontWeight: active ? 'bold' : 'normal'
    }
}

export default class TabButton extends Component {
    constructor(props) {
        super(props);

        // bind events to component
        this.handleTabChange = this.handleTabChange.bind(this);
    }

    handleTabChange(event) {
        const tabValue = event.currentTarget.value
        if(this.props.onChange) {
            this.props.onChange(tabValue)
        }
    }

    render() {
        const tabID = "tab(" + this.props.title + ")";
        return (
            <div style={styleTabButton(this.props.active)}>
                <input
                    style={styleTabButtonInput()}
                    id={ tabID }
                    type="radio"
                    name={ this.props.title }
                    value={ this.props.title }
                    checked={ this.props.active }
                    onChange={ this.handleTabChange }
                />
                <label
                    style={styleTabButtonLabel(this.props.active, this.props.padding)}
                    htmlFor={ tabID }
                >
                    { this.props.title }
                </label>
            </div>
        )
    }
}