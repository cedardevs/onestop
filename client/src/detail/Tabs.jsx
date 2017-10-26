import React, { Component } from 'react';
import FlexRow from '../common/FlexRow';

// <TabButton>

const styleTabButton = (active, first = false) => {
	return {
		display: 'flex',
		flexGrow: 1,
        border: active ? '1px solid #3E7BAD' : '1px solid #444',
        borderBottom: active ? '0' : '1px solid #3E7BAD',
		color: '#F9F9F9',
		backgroundColor: active ? '#111' : '#222',
        borderRadius: '0.618em 0.618em 0 0',
        textAlign: 'center'
	};
};

const styleTabButtonInput = () => {
	return {
		display: 'none',
	};
};

const styleTabButtonLabel = active => {
	return {
		width: '100%',
		height: '100%',
		fontWeight: active ? 'bold' : 'normal',
		fontSize: '1.3em',
		padding: '0.618em',
        textDecoration: active ? 'underline' : 'none',
        cursor: 'pointer'
    };
};

class TabButton extends Component {
	render() {
		const { first, title, value, active, onChange } = this.props;
		const tabID = `${title} - ${value}`;

		return (
			<div style={styleTabButton(active, first)}>
				<input
					style={styleTabButtonInput()}
					id={tabID}
					type="radio"
					name={title}
					value={value}
					checked={active}
					onChange={onChange}
				/>
				<label style={styleTabButtonLabel(active)} htmlFor={tabID}>
					{this.props.title}
				</label>
			</div>
		);
	}
}

// <TabButton/>

// <Tabs>

const styleTabs = {
}

const styleTabButtons = {
	flexWrap: 'nowrap',
	flexShrink: 0,
	position: 'sticky',
	top: '0',
	width: '100%',
	justifyContent: 'space-between',
};

const styleContent = {
    border: '1px solid #3E7BAD',
    borderTopWidth: '0',
    backgroundColor: "#111",
    padding: "1.618em"
}

export default class Tabs extends Component {
	constructor(props) {
		super(props);
		this.handleChange = this.handleChange.bind(this);
	}

	componentWillMount() {
		this.setState(prevState => {
			return {
				...prevState,
				activeIndex: this.props.activeIndex ? this.props.activeIndex : 0,
			};
		});
	}

	componentWillReceiveProps(nextProps) {
		if (nextProps.activeIndex !== this.props.activeIndex) {
			this.setState(prevState => {
				return {
					...prevState,
					activeIndex: this.props.activeIndex,
				};
			});
		}
	}

	handleChange(event) {
		const { onChange } = this.props;
		const index = Number(event.currentTarget.value);
		if (onChange) {
			onChange({
				activeIndex: index,
			});
		}
		this.setState(prevState => {
			return {
				...prevState,
				activeIndex: index,
			};
		});
	}

	render() {
		const { data } = this.props;
		let tabButtons = [];
		let tabContent = null;
		if (data) {
			data.forEach((tab, index) => {
				let active = false;
				if (index === this.state.activeIndex) {
					active = true;
					tabContent = tab.content;
				}
				tabButtons.push(
					<TabButton
						key={index}
						first={index === 0}
						title={tab.title}
						value={index}
						active={active}
						onChange={this.handleChange}
					/>,
				);
			});
		}
		return (
			<div style={styleTabs}>
				<FlexRow items={tabButtons} style={styleTabButtons} />
				<div style={styleContent}>
                    {tabContent}
                </div>
			</div>
		);
	}
}

// <Tabs/>
