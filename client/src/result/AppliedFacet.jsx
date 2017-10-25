import React, { Component } from 'react';

const styleFilter = {
    display: "inline-flex",
    borderRadius: "1em",
    padding: ".25em .1em .25em .5em",
    marginRight: "0.5em",
    marginBottom: "0.25em",
    backgroundColor: "teal",
    fontSize: "1.2em"
}

const styleFilterHover = {
    filter: "brightness(120%)"
}

const styleFilterFocus = {

}

const styleClose = {
    color: "lightgray",
    padding: "0 0.5em",
    cursor: "pointer"
}

export default class AppliedFacet extends Component {
	componentWillMount() {
		this.setState({
			hovering: false,
		});
	}

	handleMouseOver = event => {
		this.setState({
			hovering: true,
		});
	};

	handleMouseOut = event => {
        this.setState({
			hovering: false,
		});
	};


	render() {
	    const { category, term, onUnselect } = this.props;
        const name = term.split('>').pop().trim()

        let styleInteraction = this.state.hovering ? styleFilterHover : {}
        let styleFilterTotal = {
            ...styleFilter,
            ...styleInteraction
        }

		return (
			<span
				style={styleFilterTotal}
				onMouseOver={this.handleMouseOver}
				onMouseOut={this.handleMouseOut}
			>
				{name}
				<span style={styleClose} onClick={() => onUnselect(category, term)}>
					x
				</span>
			</span>
		);
	}
}
