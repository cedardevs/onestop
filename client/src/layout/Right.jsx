import React, {Component} from 'react'

import arrowRight from "../../img/font-awesome/white/svg/arrow-right.svg"
import arrowLeft from "../../img/font-awesome/white/svg/arrow-left.svg"

const defaultOpenText = "OPEN"
const defaultWidth = 128
const defaultBackgroundColor = '#3E7BAD'
const defaultColor = '#111'

const styleVisible = (width) => {
    return {
        color: defaultColor,
        backgroundColor: defaultBackgroundColor,
        transition: 'padding-left 0.1s linear 0s, padding-right 0.1s linear 0.1s, flex 0.5s ease-out 0.2s',
        flex: '0 0 ' + width + 'px',
        width: width + 'px',
        minWidth: "3.236em",
        position: 'relative'
    }
}

const styleHidden = (width) => {
    return {
        backgroundColor: defaultBackgroundColor,
        transition: 'flex 0.5s ease-in 0s, padding-right 0.1s linear 0.5s, padding-left 0.1s linear 0.6s',
        flex: '0 1 0',
        width: width + 'px',
        minWidth: "2em",
        position: 'relative'
    }
}

const styleHideContentArrow = {
    position: "absolute",
    top: 0,
    left: 0,
    backgroundColor: "#4B7AA8",
    paddingLeft: "0.618em",
    paddingRight: "0.618em",
    cursor: "pointer"
}

const styleHideContentArrowImage = {
    width: "2em",
    height: "42px",
}

const styleHiddenContent = {
    display: "flex",
    flexDirection: "column",
    justifyContent: "flex-start",
    alignItems: "center",
    alignSelf: "center",
    height: "100%",
    padding: "0.618em",
    cursor: "pointer"
}

const styleHiddentContentImage = {
    height: "auto",
}

const styleHiddentContentVerticalText = {
    fontSize: "1.309em",
    transform: "rotate(-90deg)",
    display: "block"
}

const verticalText = (text) => {
    return [...text].reverse().map((c, index) => {
        return <span key={index} style={styleHiddentContentVerticalText}>{c}</span>;
    });
}


export default class Right extends Component {

    componentWillMount() {
        this.setState({
            visible: this.props.visible
        });
    }

    componentWillReceiveProps(nextProps) {
        if(nextProps.visible !== this.state.visible) {
            this.setState({
                visible: nextProps.visible
            })
        }
    }

    handleOpen = (event) => {
        event.stopPropagation()
        if(!this.state.visible) {
            this.setState({
                visible: true
            })
        }
    }

    handleClose = (event) => {
        event.stopPropagation()
        if(this.state.visible) {
            this.setState({
                visible: false
            })
        }
    }

    render() {
        const width = this.props.width ? this.props.width : defaultWidth
        const classVisible = styleVisible(width)
        const classHidden = styleHidden(width)
        const classes = this.state.visible ? classVisible : classHidden
        const hideContentArrow = (
            <div style={styleHideContentArrow} onClick={this.handleClose}>
                <img style={ styleHideContentArrowImage } src={arrowRight} />
            </div>
        )
        const hiddenContent = (
            <div style={ styleHiddenContent }>
                <img style={ styleHiddentContentImage } src={arrowLeft} />
                {verticalText(this.props.openText ? this.props.openText : defaultOpenText)}
                <img style={ styleHiddentContentImage } src={arrowLeft} />
            </div>
        )
        return (
            <div style={ classes } onClick={this.handleOpen}>
                { this.state.visible ? hideContentArrow : null }
                { this.state.visible ? this.props.content : hiddenContent }
            </div>
        )
    }
}