const styles = {
  // primarily used for accessibility, to provide screen reader text for sections deliberately aria-hidden, eg: the OneStop logo
  hideOffscreen: {
    position: 'absolute',
    left: '-10000px',
    top: 'auto',
    width: '1px',
    height: '1px',
    overflow: 'hidden',
    color: 'black',
    background: 'white',
  },
}

export default styles

export const boxShadow = '1px 1px 3px rgba(50, 50, 50, 0.75)'
export const boxShadow2 = '2px 2px 4px rgba(50, 50, 50, 0.75)'

export const clipPath = 'inset(-1px -1px -3px 0px)'

// commenting items that used to be taken from styles.css (used sparsely, in case there are any lingering references)
export const COLOR_PRIMARY = '#277CB2'
// $color-primary:              #277CB2;
export const COLOR_PRIMARY_75 = 'rgba(39, 124, 178, 0.75)'
// $color-primary-75:           rgba(39, 124, 178, 0.75);
export const COLOR_PRIMARY_DARKER = '#2C3E50'
// $color-primary-darker:       #2C3E50;
export const COLOR_PRIMARY_DARKEST = '#1F2C38'
// $color-primary-darkest:      #1F2C38;

export const COLOR_PRIMARY_ALT = '#3498DB'
// $color-primary-alt:          #3498DB;
export const COLOR_PRIMARY_ALT_DARK = `darken(${COLOR_PRIMARY_ALT}, 40%)`
// $color-primary-alt-dark:     darken($color-primary-alt, 40%);
export const COLOR_PRIMARY_ALT_DARKEST = `darken(${COLOR_PRIMARY_ALT}, 70%)`
// $color-primary-alt-darkest:  darken($color-primary-alt, 70%);
export const COLOR_PRIMARY_ALT_LIGHT = '#F3736E'
// $color-primary-alt-light:    #F3736E;
export const COLOR_PRIMARY_ALT_LIGHTEST = `lighten(${COLOR_PRIMARY_ALT}, 70%)`
// $color-primary-alt-lightest: lighten($color-primary-alt, 70%);

export const COLOR_SECONDARY = '#E74C3C'
// $color-secondary:            #E74C3C;
export const COLOR_SECONDARY_DARK = '#B0392E'
// $color-secondary-dark:       #B0392E;
export const COLOR_SECONDARY_DARKEST = `darken(${COLOR_SECONDARY}, 75%)`
// $color-secondary-darkest:    darken($color-secondary, 75%);
export const COLOR_SECONDARY_LIGHT = '#3498DB'
// $color-secondary-light:      #3498DB;
export const COLOR_SECONDARY_LIGHEST = `lighten(${COLOR_SECONDARY}, 75%)`
// $color-secondary-lightest:   lighten($color-secondary, 75%);

export const COLOR_FOCUS = COLOR_PRIMARY_ALT
// $color-focus:                $color-primary-alt;
export const COLOR_VISITED = '#4c2c92'
// $color-visited:              #4c2c92;
export const COLOR_VISITED_LIGHT = '#8967d2'
// $color-visited-light:        #8967d2;

export const COLOR_SUCCESS = '#1CB841'
// $color-success:              #1CB841;

export const COLOR_GRAY = '#595959'
// $color-gray:                 #595959;
export const COLOR_GRAY_DARK = `darken(${COLOR_GRAY}, 20%)`
// $color-gray-dark:            darken($color-gray, 20%);

export const COLOR_BASE = COLOR_GRAY
// $color-base: $color-gray;
export const TEXT_MAX_WIDTH = '660px'
// $text-max-width:    660px;
export const SITE_MAX_WIDTH = '1040px'
// $site-max-width:    1040px;
export const COLOR_MEDIUM_BLUE = '#0a4595'
// $color-medium-blue:     #0a4595;
export const COLOR_GREEN = '#2e8540'
// $color-green: #2e8540;
export const COLOR_GREEN_LIGHT = '#4aa564'
// $color-green-light: #4aa564;
export const COLOR_GREEN_LIGHTER = '#94bfa2'
// $color-green-lighter: #94bfa2;
export const COLOR_GOLD = '#fdb81e'
// $color-gold: #fdb81e;

// /* Page layout */
export const DISCLAIMER_HEIGHT = '1.5em'
// $disclaimer-height: 1.5em;
export const HEADER_HEIGHT = '5.5em'
// $header-height: 5.5em;
export const FOOTER_HEIGHT = '5em'
// $footer-height: 5em;
export const SECTION_PADDING = '0.9em'
// $section-padding: .9em;

export const SiteColors = {
  WARNING: '#900303',
  VALID: '#327032',
  HEADER: '#242C36',
  HEADER_TEXT: '#FFF',
  LINK: '#2f668a',
  LINK_LIGHT: '#9fd7fc',
}
export const SiteStyles = {
  HEADER: {
    backgroundColor: SiteColors.HEADER,
    color: SiteColors.HEADER_TEXT,
  },
}

export const FilterColors = {
  //backgrounds
  MEDIUM: '#9fd7fc',
  DARK: '#18478F',
  DARKEST: '#0E274E',
  LIGHT: '#cfebfd',
  LIGHT_EMPHASIS: '#bfe4fd', // one shade darker than light
  LIGHT_SHADOW: '#8fc1e2',
  DARK_EMPHASIS: '#12347C',
  // text
  DISABLED_TEXT: '#636363',
  DISABLED_BACKGROUND: '#636363',
  INVERSE_TEXT: '#FFF',
  TEXT: 'black',
}
export const FilterStyles = {
  MEDIUM: {
    backgroundColor: FilterColors.MEDIUM,
  },
  DARK: {
    backgroundColor: FilterColors.DARK,
    color: FilterColors.INVERSE_TEXT,
  },
  LIGHT: {
    backgroundColor: FilterColors.LIGHT,
    color: FilterColors.TEXT,
  },
  DARKEST: {
    backgroundColor: FilterColors.DARKEST,
    color: FilterColors.INVERSE_TEXT,
  },
}

export const selectTheme = theme => {
  // used for consistent react-select styling
  return {
    ...theme,
    borderRadius: '0.309em',
    colors: {
      ...theme.colors,
      primary: FilterColors.DARKEST,
      primary75: FilterColors.DARK,
      primary50: FilterColors.MEDIUM,
      primary25: FilterColors.LIGHT,
      danger: '#277CB2',
      dangerLight: '#277CB2',
    },
  }
}
