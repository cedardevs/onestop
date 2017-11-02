import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { processUrl } from '../../utils/urlUtils';
import MapThumbnail from '../../common/MapThumbnail';

const styleCard = {
	width: '25em',
	height: '15.5em',
	margin: '0 2em 2em 0',
    textAlign: 'center'
};

const styleContent = {
	boxSizing: 'border-box',
	width: '100%',
	height: '100%',
	borderLeft: '2px inset rgba(0,0,0,.9)',
	borderTop: '3px inset rgba(0,0,0,.9)',
	borderBottom: '2px inset rgba(0,0,0,.9)',
	borderRight: '2px inset rgba(0,0,0,.9)',
	color: 'white',
	overflow: 'hidden',
	boxShadow: '6px 8px 5px rgba(0, 0, 0, .7)',
};

const styleOverlay = {
    position: "relative",
    display: "inline-flex",
    flexDirection: "column",
    alignItems: "flex-start",
    background: "none",
    width: "100%",
    height: "100%",
    boxSizing: "content-box",
    border: 0,
    color: "inherit",
    font: "inherit",
    lineHeight: "normal",
    overflow: "visible",
    padding: 0,
    margin: 0,

    boxShadow: "inset 1ex 4ex 1.5ex 1ex rgba(0,0,0,.8)",
}

const styleOverlayHover = {
    color: "white",
    boxShadow: "inset 1ex 4ex 1.5ex 1ex #22488A",
}

const styleOverlayFocus = {
    color: "white",
    boxShadow: "inset 1ex 4ex 1.5ex 1ex #3E97D1",
}

const styleOverlayBlur = {
    color: "inherit",
    boxShadow: "inset 1ex 4ex 1.5ex 1ex rgba(0,0,0,.8)",
}

const styleTitle = {
    position: "absolute",
    boxSizing: "border-box",
    width: "100%",
    top: 0,
    left: 0,
    right: 0,
    overflow: "hidden",
    whiteSpace: "nowrap",
    textOverflow: "ellipsis",
    fontWeight: "normal",
    fontSize: "1em",
    padding: "0.618em 1em 0.618em 1em",
    margin: 0
}

const styleTitleHover = {
    fontWeight: "bold"
}

const styleTitleFocus = {
    fontWeight: "bold"
}

const styleTitleBlur = {
    fontWeight: "normal"
}

const styleMapContainer = {
    position: "absolute",
    zIndex: -1,
    width: "100%",
    maxWidth: "100%",
    height: "100%"
}

export default class CollectionCard extends Component {

	constructor(props) {
		super(props);
		this.thumbnailUrl = processUrl(this.props.thumbnail);
	}

	componentWillMount() {
	    this.setState(prevState => {
	        return {
	            hovering: false,
                focusing: false
            }
        })
    }

    thumbnailStyle() {
        if (this.thumbnailUrl) {
            return {
                background: `url('${this.thumbnailUrl}')`,
                backgroundColor: 'black',
                backgroundRepeat: 'no-repeat',
                backgroundSize: 'cover',
                backgroundPosition: 'center center',
            };
        }
    }

    handleKeyPress(event, actionHandler) {
        if (event.key == 'Enter') {
            actionHandler();
        }
    }

    renderThumbnailMap() {
        if (!this.thumbnailUrl) {
            return (
                <div style={styleMapContainer}>
                    <MapThumbnail
                        geometry={this.props.geometry}
                        interactive={false}
                    />
                </div>
            );
        }
    }

    handleMouseOver = (event) => {
        this.setState(prevState => {
            return {
                ...prevState,
                hovering: true
            }
        })
    }

    handleMouseOut = (event) => {
        this.setState(prevState => {
            return {
                ...prevState,
                hovering: false
            }
        })
    }

    handleFocus = (event) => {
        this.setState(prevState => {
            return {
                ...prevState,
                focusing: true
            }
        })
    }

    handleBlur = (event) => {
        this.setState(prevState => {
            return {
                ...prevState,
                focusing: false
            }
        })
    }

	render() {

		const styleContentMerged = {
			...styleContent,
			...this.thumbnailStyle(),
		};

	    const styleOverlayMerged = {
            ...styleOverlay,
            ...(this.state.focusing ? styleOverlayFocus : styleOverlayBlur),
            ...(this.state.hovering ? styleOverlayHover : {})
        }

        const styleTitleMerged = {
            ...styleTitle,
            ...(this.state.focusing ? styleTitleFocus : styleTitleBlur),
            ...(this.state.hovering ? styleTitleHover : {})
        }

		return (
			<div
				style={styleCard}
				onKeyPress={e => this.handleKeyPress(e, this.props.onClick)}
			>
				<div style={styleContentMerged}>
					<button style={styleOverlayMerged} onClick={() => this.props.onClick()} onMouseOver={this.handleMouseOver} onMouseOut={this.handleMouseOut} onFocus={this.handleFocus} onBlur={this.handleBlur}>
						<h2 style={styleTitleMerged}>{this.props.title}</h2 >
						{this.renderThumbnailMap()}
					</button>
				</div>
			</div>
		);
	}
}

CollectionCard.propTypes = {
    onClick: PropTypes.func.isRequired,
    title: PropTypes.string.isRequired,
    thumbnail: PropTypes.string,
    geometry: PropTypes.object,
};