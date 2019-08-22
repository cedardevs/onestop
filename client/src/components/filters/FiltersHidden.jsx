import React from 'react'
import Button from '../common/input/Button'
import arrowRight from '../../../img/font-awesome/white/svg/arrow-right.svg'
import {fontFamilySerif} from '../../utils/styleUtils'

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
  lineHeight: '0.72em',
  display: 'block',
}

class FiltersHidden extends React.Component {
  render() {
    const {openLeft, text} = this.props

    const verticalChars = [ ' ', ...text, ' ' ].reverse().map((c, i) => {
      // when the characters are empty spaces, we use the unicode `\u00A0` character
      // so that JSX knows how to handle a whitespace without collapsing it or treating
      // other attempts like &nbsp; as literal text
      return (
        <span
          key={`verticalChar${i}`}
          aria-hidden="true"
          style={styleHiddentContentVerticalText}
        >
          {/\s/.test(c) ? '\u00A0' : c}
        </span>
      )
    })

    const buttonHide = (
      <Button
        style={styleHiddenContent}
        styleIcon={{width: '1em', height: 'initial'}}
        onClick={() => {
          openLeft()
        }}
        title={`Show ${text} Menu`}
        ariaExpanded={false}
        styleHover={{
          background: 'linear-gradient(90deg, #277CB2, #28323E)',
        }}
      >
        <img
          style={styleHiddentContentImage}
          aria-hidden="true"
          alt={`Show ${text} Menu`}
          src={arrowRight}
        />
        {verticalChars}
        <img
          style={styleHiddentContentImage}
          aria-hidden="true"
          alt={`Show ${text} Menu`}
          src={arrowRight}
        />
      </Button>
    )

    return buttonHide
  }
}

export default FiltersHidden
