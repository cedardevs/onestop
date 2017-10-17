import React, { Component } from 'react';

const styleContainer = {
	display: 'flex',
};

const styleIcon = {
	display: 'flex',
	alignItems: 'center',
	marginRight: '0.616em',
	width: '2em',
	height: '2em',
};

const styleIconImage = {
	width: '100%',
	height: '100%',
};

const styleText = {
	width: '100%',
	alignSelf: 'center',
};

export default class FilterHeading extends Component {
	render() {
		return (
			<div style={{ ...styleContainer, ...this.props.style }}>
				<div style={styleIcon}>
					<img
						style={styleIconImage}
						src={this.props.icon}
						alt={`${this.props.text} Icon`}
					/>
				</div>
				<div style={styleText}>{this.props.text}</div>
			</div>
		);
	}
}
