import React from 'react'
import Expandable from '../../common/ui/Expandable'
import Checkbox from '../../common/input/Checkbox'

/**
  This component is a node in the facet tree.
**/

const styleHideFocus = {
  outline: 'none', // The focused state is being passed to a child component to display
}

export default class FacetTreeItem extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      focusing: false,
    }
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    this.setState(prevState => {
      return {
        ...prevState,
        open: nextProps.open,
        selected: nextProps.selected,
      }
    })
  }

  handleFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {
      facetId,
      category,
      term,
      count,
      children,
      hasChildren,
      tabIndex,
      keyword,
      facetMap,
      disabled,
    } = this.props
    const {open, selected, focusing} = this.state

    const label = `${keyword} (${count})`

    const styleFacetContainer =
      (tabIndex === '0' || tabIndex === 0) && focusing
        ? this.props.styleFocus
        : {}
    const styleFocus = this.props.styleFocus ? styleHideFocus : {}

    const facet = (
      <div style={styleFacetContainer}>
        <Checkbox
          label={label}
          id={`checkbox-${facetId}`}
          disabled={disabled}
          tabIndex={-1}
          checked={selected}
          value={{term: term, category: category, id: facetId}}
          onChange={this.props.handleSelectToggleMouse}
          styleFocus={this.props.styleCheckboxFocus}
        />
      </div>
    )

    const content = hasChildren ? (
      <Expandable
        open={open}
        value={facetMap}
        heading={facet}
        tabbable={false}
        styleHeading={this.props.styleFacet}
        styleContent={this.props.styleChildren}
        styleFocus={styleFocus}
        styleArrowFocus={{outline: 'none'}}
        showArrow={true}
        content={<div role="group">{children}</div>}
        onToggle={this.props.handleExpandableToggle}
      />
    ) : (
      <div style={this.props.styleFacet}>{facet}</div>
    )

    const ariaLabel = `${label} match${count > 1 ? 'es' : ''}`

    // if there are no children, undefined prevents the property from appearing on the element
    const ariaExpanded = hasChildren ? open : undefined

    return (
      <div
        key={facetId}
        tabIndex={tabIndex}
        style={styleFocus}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        role="treeitem"
        id={facetId}
        aria-label={ariaLabel}
        aria-expanded={ariaExpanded}
        aria-selected={selected}
        aria-disabled={disabled}
        aria-setsize={this.props.setSize}
        aria-posinset={this.props.posInSet}
      >
        {content}
      </div>
    )
  }
}
