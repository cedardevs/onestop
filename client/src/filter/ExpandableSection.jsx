import React, { Component } from 'react';
import PropTypes from 'prop-types';

const styleExpandableSection = {
	userSelect: 'none'
};

const styleExpandableSectionHeading = {
	padding: '0.616em',
	textAlign: 'left',
	marginBottom: '1px',
	display: 'flex',
	alignItems: 'center',
	// backgroundColor: '#2271a9',
    color: '#FFFFFF',
	fontSize: '13px'
};

const styleExpandableSubsection = {
	// backgroundColor: 'white',
	// color: '#000000'
};

const styleExpandableSectionHeadingContentHidden = {
	borderBottom: 0
};

const styleExpandableSectionHeadingImage = {
	width: '20px',
	height: '100%'
};

const styleExpandableSectionHeadingText = {
	marginLeft: '0.616em',
	lineHeight: '100%',
	height: '100%',
	position: 'relative'
};

const styleExpandableSectionHeadingTextHover = {
	position: 'absolute',
	top: '100px',
	left: '100px',
	backgroundColor: 'cyan'
};


const styleExpandableSectionContent = {
	textAlign: 'left',
	marginLeft: '1em',
	maxHeight: 0,
	transition: 'max-height 0.15s ease-out',
	overflow: 'hidden',
	fontSize: '12px'
};

const styleExpandableSectionContentShown = {
	maxHeight: '10000px',
	transition: 'max-height 0.25s ease-in'
};

const styleExpandableSectionContentHidden = {
};

export default class ExpandableSection extends Component {
	constructor(props) {
		super(props);
		this.state = { open: props.open, heading: props.heading, hovering: false };

		this.handleClick = this.handleClick.bind(this);
		this.handleMouseOver = this.handleMouseOver.bind(this);
		this.handleMouseOut = this.handleMouseOut.bind(this);
		this.handleCheckboxClick = this.handleCheckboxClick.bind(this);
	}

	handleClick() {
		this.setState(prevState => ({
			open: !prevState.open,
			heading: prevState.heading,
			hovering: prevState.hovering
		}));
	}

	handleMouseOver() {
		this.setState(prevState => ({
			open:  prevState.open,
			heading: this.props.term ? this.props.term : this.props.heading,
			hovering: true
		}))
	}

	handleMouseOut() {
		this.setState(prevState => ({
			open: prevState.open,
			heading: this.props.heading,
			hovering: false
		}))
	}

    handleCheckboxClick(event) {
		// TODO: decouple this from filter specific implementation
		// prevent parent click from propagating (only fire onClick of checkbox (not parent filter section onClick too)
		event.stopPropagation();
	}

	handleCheckboxChange(event) {
	}

	render() {
		const treeArrow = this.state.open ? <span>&nbsp;&#9660;</span> : <span>&nbsp;&#9654;</span>;
		const checkbox = this.props.isSubsection ? <input type="checkbox" id="subscribeNews" name="subscribe" value="newsletter" onClick={this.handleCheckboxClick} onChange={this.handleCheckboxChange} /> : null;

		const styleHeadingHideContent = this.state.open
			? {}
			: styleExpandableSectionHeadingContentHidden;
		const styleContent = this.state.open
			? styleExpandableSectionContentShown
			: styleExpandableSectionContentHidden;

		const styleSubsection = this.props.isSubsection ? styleExpandableSubsection : {};

		const tooltip = (
			<span style={styleExpandableSectionHeadingTextHover}>
				{this.props.term ? this.props.term : this.props.heading}
			</span>
		);

		return (
			<div style={styleExpandableSection}>
				<div
					style={{
						...styleExpandableSectionHeading,
						...styleHeadingHideContent,
						...styleSubsection
					}}
					onClick={this.handleClick}
					onMouseOver={this.handleMouseOver}
					onMouseOut={this.handleMouseOut}
				>
                    {checkbox}
					<span style={styleExpandableSectionHeadingText}>
						{this.props.heading} {this.props.count ? "(" + this.props.count + ")" : ""}
					</span>
					{ this.props.isLeaf ? "" : treeArrow }
				</div>
				<div style={{ ...styleExpandableSectionContent, ...styleContent }}>
					{this.props.content}
				</div>
			</div>
		);
	}
}
