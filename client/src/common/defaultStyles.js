const styles = {
  // primarily used for accessibility, to provide screen reader text for sections deliberately aria-hidden, eg: the OneStop logo
  hideOffscreen: {
    position: 'absolute',
    left: '-10000px',
    top: 'auto',
    width: '1px',
    height: '1px',
    overflow: 'hidden',
  },
}

export default styles

export const boxShadow = '1px 1px 3px rgba(50, 50, 50, 0.75)'
