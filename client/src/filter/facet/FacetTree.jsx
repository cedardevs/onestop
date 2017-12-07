import React from 'react'
import Facet from './Facet'
import _ from 'lodash'
import {Key} from '../../utils/keyboardUtils'

/**
  This component contains the content of a facet category. It is essentially a
  speciallized tree menu.
**/

const styleFacet = backgroundColor => {
  return {
    padding: '0.618em',
    backgroundColor: backgroundColor ? backgroundColor : 'initial',
    color: '#FFF',
    display: 'flex',
    textAlign: 'left',
    alignItems: 'center',
  }
}

const styleRovingFocus = {
  boxShadow: '0 0 0 1px #FFF',
}

const styleRovingFocusCheckbox = {
  outline: 'none',
  boxShadow: '0 0 2px 2px #12347C',
}

const styleExpandableContent = marginNest => {
  return {
    marginLeft: marginNest ? marginNest : '1em',
  }
}

export default class FacetTree extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      facetMap: {},
      allFacetsInOrder: [],
      facetLookup: {},
    }
  }

  componentDidMount(nextProps) {
    this.setState(prevState => {
      return {
        ...prevState,
        // reset facet references before reparsing the map, which should populate them again
        allFacetsInOrder: [],
        facetLookup: {},
      }
    })
    if (!_.isEqual({}, this.props.facetMap)) {
      // parse map with the top layer being marked as visible: true.
      // note this function uses side effects to alter the facetMap itself,
      // then we simply store that in state
      this.parseMap(this.props.facetMap, 1)
      this.setState(prevState => {
        let facetMap = Object.assign({}, prevState.facetMap)
        facetMap = this.props.facetMap
        return {
          ...prevState,
          facetMap: facetMap,
        }
      })
    }
  }

  handleExpandableToggle = event => {
    this.setState(prevState => {
      let node = this.state.facetLookup[event.value]
      if (node) {
        node.open = event.open
        let updateVisibility = (children, visibility) => {
          _.each(children, (value, key) => {
            value.visible = visibility
            updateVisibility(value.children, visibility && value.open)
          })
        }
        updateVisibility(node.children, node.open)

        return {
          ...prevState,
          facetMap: this.state.facetMap, // editing to node directly modified this, this is to trigger the rest of the stack
        }
      }
    })
  }

  updateRovingIndex = facetId => {
    this.setState(prevState => {
      const oldIndex = prevState.facetLookup[prevState.rovingIndex]
      oldIndex.tabIndex = '-1'

      const newIndex = prevState.facetLookup[facetId]
      newIndex.tabIndex = '0'

      return {
        ...prevState,
        facetMap: this.state.facetMap, // editing to node directly modified this, this is to trigger the rest of the stack
        rovingIndex: facetId,
      }
    })
    document.getElementById(facetId).focus()
  }

  triggerRight = () => {
    const id = this.state.rovingIndex
    const node = this.state.facetLookup[id]
    if (!node.open) {
      // open node
      this.handleExpandableToggle({open: true, value: id})
    }
    else {
      // move focus to first child
      const facetId = node.relations.children[0]
      if (facetId) {
        this.updateRovingIndex(facetId)
      }
    }
  }

  triggerLeft = () => {
    const id = this.state.rovingIndex
    const node = this.state.facetLookup[id]
    if (node.open) {
      // close node
      this.handleExpandableToggle({open: false, value: id})
    }
    else {
      // move focus to parent
      const facetId = node.relations.parent
      if (facetId) {
        this.updateRovingIndex(facetId)
      }
    }
  }

  moveFocusDown = () => {
    const id = this.state.rovingIndex
    const orderIndex = _.indexOf(this.state.allFacetsInOrder, id)

    if (orderIndex < this.state.allFacetsInOrder.length - 1) {
      const nextVisible = _.find(
        this.state.allFacetsInOrder,
        facetId => {
          return this.state.facetLookup[facetId].visible
        },
        orderIndex + 1
      )

      if (nextVisible) {
        this.updateRovingIndex(nextVisible)
      }
    }
  }

  moveFocusToEnd = () => {
    const nextVisible = _.findLast(this.state.allFacetsInOrder, facetId => {
      return this.state.facetLookup[facetId].visible
    })

    if (nextVisible) {
      this.updateRovingIndex(nextVisible)
    }
  }

  moveFocusToStart = () => {
    this.updateRovingIndex(this.state.allFacetsInOrder[0])
  }

  moveFocusUp = () => {
    const id = this.state.rovingIndex
    const orderIndex = _.indexOf(this.state.allFacetsInOrder, id)
    if (orderIndex > 0) {
      const nextVisible = _.findLast(
        this.state.allFacetsInOrder,
        facetId => {
          return this.state.facetLookup[facetId].visible
        },
        orderIndex - 1
      )

      this.updateRovingIndex(nextVisible)
    }
  }
  //
  // isSelected = (category, term) => {
  //   const selectedTerms = this.props.selectedFacets[category]
  //   return selectedTerms ? selectedTerms.includes(term) : false
  // }

  handleSelectToggleMouse = e => {
    this.props.handleSelectToggle(e.value, e.checked)
  }

  createFacetComponent = (facet, parent, siblings) => {
    // handle any nulls that might get into this function
    let facetComponent = null
    if (!facet) {
      return facetComponent
    }

    if ('children' in facet) {
      const hasChildren = !_.isEmpty(facet.children)
      const children = hasChildren
        ? this.createFacetComponent(facet.children, facet)
        : null

      return (
        <Facet
          facetId={facet.id}
          key={facet.id}
          category={facet.category}
          term={facet.term}
          count={facet.count}
          open={facet.open}
          selected={this.isSelected(facet.category, facet.term)}
          tabIndex={facet.tabIndex}
          focused={this.state.focus}
          children={children}
          hasChildren={hasChildren}
          handleSelectToggleMouse={this.handleSelectToggleMouse}
          handleExpandableToggle={this.handleExpandableToggle}
          styleFacet={styleFacet(this.props.backgroundColor)}
          styleFocus={styleRovingFocus}
          styleCheckboxFocus={styleRovingFocusCheckbox}
          styleChildren={styleExpandableContent(this.props.marginNest)}
        />
      )
    }
    else {
      // for each key recurse
      return Object.keys(facet).map(subFacet =>
        // TODO console.log when this happens??
        this.createFacetComponent(facet[subFacet])
      )
    }
  }
  //
  // parseMap = (map, level, parentOpen, parentId) => {
  //   if (_.isEqual({}, map)) {
  //     // cannot parse empty map
  //     return []
  //   }
  //
  //   _.each(map, (value, key) => {
  //     value.relations = {}
  //     value.open = false // always default to everything collapsed
  //     value.tabIndex = '-1'
  //   })
  //
  //   if (level === 1) {
  //     // id first layer to set the initial tab focus
  //     const value = _.map(map, (value, key) => value)[0]
  //     value.tabIndex = '0'
  //     this.setState(prevState => {
  //       return {
  //         ...prevState,
  //         rovingIndex: value.id,
  //       }
  //     })
  //   }
  //
  //   _.each(map, (value, key) => {
  //     this.setState(prevState => {
  //       // update state that lets us quickly traverse the nodes in up/down order
  //       let allFacetsInOrder = Object.assign([], prevState.allFacetsInOrder)
  //       allFacetsInOrder.push(value.id)
  //
  //       // update state that lets us set focus to another node or update visibility, since it is a property that combines the state of several nodes
  //       let facetLookup = Object.assign({}, prevState.facetLookup)
  //       facetLookup[value.id] = value
  //
  //       return {
  //         ...prevState,
  //         allFacetsInOrder: allFacetsInOrder,
  //         facetLookup: facetLookup,
  //       }
  //     })
  //
  //     value.relations.parent = parentId
  //     value.relations.children = this.parseMap(
  //       value.children,
  //       level + 1,
  //       parentOpen && value.open,
  //       value.id
  //     )
  //     value.visible = level === 1 || !!parentOpen
  //   })
  //
  //   return _.map(map, (value, key) => value.id) // return siblings
  // }

  handleKeyPressed = e => {
    // do nothing if modifiers are pressed
    if (e.metaKey || e.shiftKey || e.ctrlKey || e.altKey) {
      return
    }

    e.stopPropagation()

    if (e.keyCode === Key.SPACE || e.keyCode === Key.ENTER) {
      e.preventDefault() // prevent scrolling down on space press
      const {facetId, category, term} = this.state.facetLookup[
        this.state.rovingIndex
      ]
      const selected = this.isSelected(category, term)
      this.props.handleSelectToggle(
        {id: facetId, category: category, term: term},
        !selected
      )
    }
    if (e.keyCode === Key.HOME) {
      this.moveFocusToStart()
    }
    if (e.keyCode === Key.END) {
      this.moveFocusToEnd()
    }
    if (e.keyCode === Key.UP) {
      e.preventDefault() // prevent scrolling behavior
      this.moveFocusUp()
    }
    if (e.keyCode === Key.DOWN) {
      e.preventDefault() // prevent scrolling behavior
      this.moveFocusDown()
    }
    if (e.keyCode === Key.LEFT) {
      this.triggerLeft()
    }
    if (e.keyCode === Key.RIGHT) {
      this.triggerRight()
    }
  }

  handleKeyDown = e => {
    // prevent the default behavior for tree control keys
    // these are the control keys used by the tree menu
    const treeControlKeys = [
      Key.SPACE,
      Key.ENTER,
      Key.HOME,
      Key.END,
      Key.UP,
      Key.DOWN,
      Key.LEFT,
      Key.RIGHT,
    ]
    if (
      !e.metaKey &&
      !e.shiftKey &&
      !e.ctrlKey &&
      !e.altKey &&
      treeControlKeys.includes(e.keyCode)
    ) {
      e.preventDefault()
    }
  }

  render() {
    const facetHierarchy = this.createFacetComponent(this.state.facetMap)

    return (
      <div
        role="tree"
        aria-labelledby={this.props.headerId}
        aria-multiselectable="true"
        onKeyUp={this.handleKeyPressed} // onKeyDown isnt an actual keypress
        onKeyDown={this.handleKeyDown}
      >
        {facetHierarchy}
      </div>
    )
  }
}
