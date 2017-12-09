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
      facetList: [],
      hierarchy:[],
      rovingIndex: null,
    }
  }

  componentDidMount() {

    this.setState(prevState => {
      // do with new facet map only (init state)
      const facets = this.props.facetMap // TODO rename facetMap to facetList?
      _.each(facets, (facet) => {
        // TODO replace with map merge
        facet.open = false
        facet.tabIndex = '-1'
      })
      const firstFocused = facets[0]
      firstFocused.tabIndex = 0

      const hierarchy = this.props.hierarchy
      _.each(hierarchy, (facetInMap) => {
        this.updateNodeVisibility(facetInMap, open)
      })
      return {
        ...prevState,
        facetList: facets,
        hierarchy: hierarchy,
        rovingIndex: firstFocused.id,
      }
    })
  }

  facetLookup2 = (id) => { // TODO rename
    return _.find(this.state.facetList, (facet) => {
      return facet.id === id
    })
  }

  updateNodeVisibility = (facetInMap, open) => {
    let node = this.facetLookup2(facetInMap.id)
    if(!node) {return}
    node.open = open
    let updateVisibility = (children, visibility) => { // TODO verify this all updated correctly....
      _.each(children, (value, key) => {
        value.visible = visibility
        updateVisibility(value.children, visibility && value.open)
      })
    }
    updateVisibility(facetInMap.children, node.open)
  }

  handleExpandableToggle = event => { // TODO this needs the facetInMap to work right
    console.log('toggle', event)
    this.updateNodeVisibility(event.value, event.open)
    this.setState(prevState => {

      // let node = this.facetLookup2[event.value]
      // if (node) {
      //   node.open = event.open
      //   let updateVisibility = (children, visibility) => {
      //     _.each(children, (value, key) => {
      //       value.visible = visibility
      //       updateVisibility(value.children, visibility && value.open)
      //     })
      //   }
      //   updateVisibility(node.children, node.open)
      //
      //   return {
      //     ...prevState,
      //     facetMap: this.state.facetMap, // editing to node directly modified this, this is to trigger the rest of the stack
      //   }
      // }

      return {
        ...prevState,
        hierarchy: prevState.hierarchy, // TODO needed?
      }
    })
  }

  updateRovingIndex = facetId => { // TODO next up - all the key binding stuff with the new data structure(s)
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

  createFacetComponent = (facetInMap) => {
    // if (!facet || _.isEqual({}, facet)) { // TODO why is this reaching this part ???
    //   // cannot parse empty map
    //   return []
    // }
    // handle any nulls that might get into this function
    const facet = this.facetLookup2(facetInMap.id)
    // console.log(facetInMap, facet)
    let facetComponent = null
    if (!facet) {
      return facetComponent
    }
    const facetChildren = _.map(facetInMap.children, facet => this.createFacetComponent(facet))
    // if (!Array.isArray(facet)) {
      const hasChildren = !_.isEmpty(facetInMap.children)
      const children = hasChildren
        ? facetChildren//this.createFacetComponent(facet.children, facet)
        : null

      return (
        <Facet
          facetId={facet.id}
          facetMap={facetInMap}
          key={facet.id}
          category={facet.category}
          term={facet.term}
          count={facet.count}
          open={facet.open}
          keyword={facet.keyword}
          selected={facet.selected}
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
    // }
    // else {
    //   // for each key recurse
    //   // return Object.keys(facet).map(subFacet =>
    //   //   // TODO console.log when this happens??
    //   //   this.createFacetComponent(facet[subFacet])
    //   // )
    //   // _.each(facet, facet=> {
    //   //   console.log('each facet is',facet)
    //   // })
    //   return _.map(facet, facet => this.createFacetComponent(facet))
    // }
    return facetChildren// _.map(facetInMap.children, facet => this.createFacetComponent(facet))
  }
  //
  parseMap = (map, level, parentOpen, parentId) => {
    if (!map || _.isEqual({}, map)) {
      // cannot parse empty map
      return []
    }

    _.each(map, (facet) => {
      // value.relations = {}
      // console.log('parseMap part the 1', value)
      facet.open = false // always default to everything collapsed
      facet.tabIndex = '-1'
    })

    if (level === 1) {
      // id first layer to set the initial tab focus
      const value = _.map(map, (value) => value)[0]
      value.tabIndex = '0'
      this.setState(prevState => {
        return {
          ...prevState,
          rovingIndex: value.id,
        }
      })
    }

    _.each(map, (value) => {
      this.setState(prevState => {
        // update state that lets us quickly traverse the nodes in up/down order
        let allFacetsInOrder = Object.assign([], prevState.allFacetsInOrder)
        allFacetsInOrder.push(value.id)

        // update state that lets us set focus to another node or update visibility, since it is a property that combines the state of several nodes
        let facetLookup = Object.assign({}, prevState.facetLookup)
        facetLookup[value.id] = value

        return {
          ...prevState,
          allFacetsInOrder: allFacetsInOrder,
          facetLookup: facetLookup,
        }
      })

      // value.relations.parent = parentId
      // value.relations.children = this.parseMap(
      //   value.children,
      //   level + 1,
      //   parentOpen && value.open,
      //   value.id
      // )
      value.visible = level === 1 || !!parentOpen
    })

    return _.map(map, (value, key) => value.id) // return siblings
  }

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
    const facetHierarchy = _.map(this.props.hierarchy, (facet) => {return this.createFacetComponent(facet)})
    // this.createFacetComponent(this.props.hierarchy)

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
