import React, { Component } from 'react';
import PropTypes from 'prop-types';

const styleFilterMenuSection = {
	userSelect: 'none'
};

const styleFilterMenuSectionHeading = {
	padding: '0.616em',
	textAlign: 'left',
	marginBottom: '1px',
	display: 'flex',
	alignItems: 'center',
	backgroundColor: '#2271a9',
    color: '#FFFFFF',
	fontSize: '13px'
};

const styleFilterMenuSubsection = {
	backgroundColor: 'white',
	color: '#000000'
};

const styleFilterMenuSectionHeadingContentHidden = {
	borderBottom: 0
};

const styleFilterMenuSectionHeadingImage = {
	width: '20px',
	height: '100%'
};

const styleFilterMenuSectionHeadingText = {
	marginLeft: '0.616em',
	lineHeight: '100%',
	height: '100%'
};

const styleFilterMenuSectionContent = {
	textAlign: 'left',
	marginLeft: '1em',
	maxHeight: 0,
	transition: 'max-height 0.15s ease-out',
	overflow: 'hidden',
	fontSize: '12px'
};

const styleFilterMenuSectionContentShown = {
	maxHeight: '10000px',
	transition: 'max-height 0.25s ease-in'
};

const styleFilterMenuSectionContentHidden = {
};

export default class FilterSection extends Component {
	// static propTypes = {
	// 	/** Switch the filter section open (true) or closed (false) */
	// 	open: PropTypes.bool,
	// 	/** Handler for when the FilterSection is opened and closed */
	// 	onChange: PropTypes.func,
	// 	/** Heading (React element) which changes the open state on click */
	// 	heading: PropTypes.string,
	// 	/** Content (React element) to display when section is open */
	// 	content: PropTypes.element,
	// };
	// static defaultProps = {
	// 	open: false,
	// 	onChange: event => {
	// 		console.log('FilterSection::onChange::event:', event);
	// 	},
	// 	heading: 'Section Heading',
	// 	content: <p>Section Content</p>,
	// };

	constructor(props) {
		super(props);
		this.state = { open: props.open, heading: props.heading };

		this.handleClick = this.handleClick.bind(this);
		this.handleMouseOver = this.handleMouseOver.bind(this);
		this.handleMouseOut = this.handleMouseOut.bind(this);
		this.handleCheckboxClick = this.handleCheckboxClick.bind(this);
	}

	handleClick() {
		// this.props.onChange({
		// 	open: !this.state.open,
		// });
		this.setState(prevState => ({
			open: !prevState.open,
			heading: prevState.heading
		}));
	}

	handleMouseOver() {
		console.log("this.props:", this.props);
		this.setState(prevState => ({
			open:  prevState.open,
			heading: this.props.term ? this.props.term : this.props.heading
		}))
	}

	handleMouseOut() {
		this.setState(prevState => ({
			open: prevState.open,
			heading: this.props.heading
		}))
	}

    handleCheckboxClick(event) {
		// prevent parent click from propagating (only fire onClick of checkbox (not parent filter section onClick too)
		event.stopPropagation();
		console.log("ON CHECKBOX CLICK");
	}

	handleCheckboxChange(event) {
		console.log("ON CHECKBOX CHANGE");
	}

	render() {
		const treeArrow = this.state.open ? <span>&#9660;</span> : <span>&#9654;</span>;
		const checkbox = this.props.isSubsection ? <input type="checkbox" id="subscribeNews" name="subscribe" value="newsletter" onClick={this.handleCheckboxClick} onChange={this.handleCheckboxChange} /> : null;

		const styleHeadingHideContent = this.state.open
			? {}
			: styleFilterMenuSectionHeadingContentHidden;
		const styleContent = this.state.open
			? styleFilterMenuSectionContentShown
			: styleFilterMenuSectionContentHidden;

		const styleSubsection = this.props.isSubsection ? styleFilterMenuSubsection : {};

		return (
			<div style={styleFilterMenuSection}>
				<div
					style={{
						...styleFilterMenuSectionHeading,
						...styleHeadingHideContent,
						...styleSubsection
					}}
					onClick={this.handleClick}
					onMouseOver={this.handleMouseOver}
					onMouseOut={this.handleMouseOut}
				>
                    {checkbox}
					<span style={styleFilterMenuSectionHeadingText}>
						{this.state.heading} {this.props.count ? "(" + this.props.count + ")" : ""}
					</span>
					&nbsp;{treeArrow}
				</div>
				<div style={{ ...styleFilterMenuSectionContent, ...styleContent }}>
					{this.props.content}
				</div>
			</div>
		);
	}
}
