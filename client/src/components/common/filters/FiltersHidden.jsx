import React, {Component} from 'react'
import Button from '../input/Button'
import arrowRight from '../../../../img/font-awesome/white/svg/arrow-right.svg'
import {fontFamilySerif} from '../../../utils/styleUtils'

const styleHiddenContent = {
  background: '#277CB2', // $color_primary
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'flex-start',
  alignItems: 'center',
  alignSelf: 'center',
  height: '100%',
  cursor: 'pointer',
  border: 0,
  borderRadius: 0,
  padding: '.25em',
  width: '1.5em',
}

const styleHiddentContentImage = {
  height: '1em',
}

const styleHiddentContentVerticalText = {
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  transform: 'rotate(-90deg)',
  lineHeight: '1em',
  display: 'block',
}

class FiltersHidden extends Component {
  render() {
    const {openLeft} = this.props

    const buttonHide = (
      <Button
        style={styleHiddenContent}
        styleIcon={{width: '1em', height: 'initial'}}
        onClick={() => {
          openLeft()
        }}
        title={'Show Filter Menu'}
        ariaExpanded={false}
        styleHover={{
          background: 'linear-gradient(90deg, #277CB2, #28323E)',
        }}
      >
        <img
          style={styleHiddentContentImage}
          aria-hidden="true"
          alt="Show Filter Menu"
          src={arrowRight}
        />
        <span aria-hidden="true" style={styleHiddentContentVerticalText}>
          S
        </span>
        <span aria-hidden="true" style={styleHiddentContentVerticalText}>
          R
        </span>
        <span aria-hidden="true" style={styleHiddentContentVerticalText}>
          E
        </span>
        <span aria-hidden="true" style={styleHiddentContentVerticalText}>
          T
        </span>
        <span aria-hidden="true" style={styleHiddentContentVerticalText}>
          L
        </span>
        <span aria-hidden="true" style={styleHiddentContentVerticalText}>
          I
        </span>
        <span aria-hidden="true" style={styleHiddentContentVerticalText}>
          F
        </span>
        <img
          style={styleHiddentContentImage}
          aria-hidden="true"
          alt="Show Filter Menu"
          src={arrowRight}
        />
      </Button>
    )

    return buttonHide
  }
}

export default FiltersHidden
