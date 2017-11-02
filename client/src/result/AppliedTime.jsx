import React, { Component } from 'react';

const styleDefault = {
    display: 'inline-flex',
    borderRadius: '0.1em 0.4em',
    padding: '.25em .1em .25em .5em',
    marginRight: '0.5em',
    marginBottom: '0.25em',
    backgroundColor: 'maroon',
    fontSize: '1.2em',
};

const styleHoverDefault = {
    filter: 'brightness(120%)',
};

const styleFocusDefault = {
    filter: 'brightness(120%)',
};

const styleClose = {
    color: 'lightgray',
    padding: '0 0.5em',
    cursor: 'pointer',
};

export default class AppliedTime extends Component {
    componentWillMount() {
        this.setState({
            hovering: false,
            focusing: false,
        });
    }

    handleMouseOver = event => {
        this.setState(prevState => {
            return {
                ...prevState,
                hovering: true,
            };
        });
    };

    handleMouseOut = event => {
        this.setState(prevState => {
            return {
                ...prevState,
                hovering: false,
            };
        });
    };

    handleFocus = event => {
        this.setState(prevState => {
            return {
                ...prevState,
                focusing: true,
            };
        });
    };

    handleBlur = event => {
        this.setState(prevState => {
            return {
                ...prevState,
                focusing: false,
            };
        });
    };

    render() {
        const {
            label,
            dateTime,
            onUnselect,
            style,
            styleHover,
            styleFocus,
        } = this.props;
        const name = term
            .split('>')
            .pop()
            .trim();

        const stylesMerged = {
            ...styleDefault,
            ...style,
            ...(this.state.hovering ? { ...styleHoverDefault, styleHover } : {}),
            ...(this.state.focusing ? { ...styleFocusDefault, styleFocus } : {}),
        };

        return (
            <span
                style={stylesMerged}
                onMouseOver={this.handleMouseOver}
                onMouseOut={this.handleMouseOut}
                onFocus={this.handleFocus}
                onBlur={this.handleBlur}
            >
                {label} {dateTime}
                <span style={styleClose} onClick={onUnselect}>
					x
				</span>
			</span>
        );
    }
}
