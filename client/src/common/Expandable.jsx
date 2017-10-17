import React, { Component } from 'react';

const style = {
	userSelect: 'none',
};

const styleHeading = {
	display: 'flex',
	textAlign: 'left',
	alignItems: 'center',
	color: '#FFFFFF',
	cursor: 'pointer',
};

const styleHeadingShown = {};

const styleHeadingHidden = {
	borderBottom: 0,
};

const styleHeadingContent = {};

const styleArrow = {};

const styleContent = {
	textAlign: 'left',
	overflow: 'hidden',
};

const styleContentShown = {
	maxHeight: '10000px',
	transition: 'max-height 1.25s ease-in',
};

const styleContentHidden = {
	maxHeight: 0,
	transition: 'max-height 1.25s ease-out',
	transitionDelay: '-1s',
};

export default class Expandable extends Component {
	constructor(props) {
		super(props);
		this.state = {
			open: props.open,
			hovering: false,
			showArrow: props.showArrow,
		};

		this.handleClick = this.handleClick.bind(this);
		this.handleMouseOver = this.handleMouseOver.bind(this);
		this.handleMouseOut = this.handleMouseOut.bind(this);
	}

	componentWillReceiveProps(nextProps) {
		if (nextProps.open !== this.state.open) {
			this.setState({
				...this.state,
				open: nextProps.open,
			});
		}
	}

	handleClick() {
		this.setState(prevState => {
			const newOpen = !prevState.open;
			if (this.props.onToggle) {
				this.props.onToggle({ open: newOpen, value: this.props.value });
			}
			return {
				...prevState,
				open: newOpen,
			};
		});
	}

	handleMouseOver() {
		this.setState(prevState => ({
			...prevState,
			hovering: true,
		}));
	}

	handleMouseOut() {
		this.setState(prevState => ({
			...prevState,
			hovering: false,
		}));
	}

	render() {
		const arrow = this.props.showArrow ? (
			this.state.open ? (
				<span>&nbsp;&#9660;</span>
			) : (
				<span>&nbsp;&#9654;</span>
			)
		) : null;

		const styleHeadingHide = this.state.open
			? styleHeadingShown
			: styleHeadingHidden;
		const styleContentVisibility = this.state.open
			? styleContentShown
			: styleContentHidden;

		return (
			<div style={style}>
				<div
					style={{
						...styleHeading,
						...this.props.styleHeading,
						...styleHeadingHide,
					}}
					onClick={this.handleClick}
					onMouseOver={this.handleMouseOver}
					onMouseOut={this.handleMouseOut}
				>
					<div style={styleHeadingContent}>{this.props.heading}</div>
					<div style={styleArrow}>{arrow}</div>
				</div>

				<div
					style={{
						...styleContent,
						...this.props.styleContent,
						...styleContentVisibility,
					}}
				>
					{this.props.content}
				</div>
			</div>
		);
	}
}
