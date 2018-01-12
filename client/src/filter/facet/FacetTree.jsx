import React from 'react'
import Facet from './Facet'
import _ from 'lodash'
import {Key} from '../../utils/keyboardUtils'

import Immutable from 'seamless-immutable'


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

  mergeChildren = (list1, list2) => { // custom hierarchy list merging...
    let list = []
    _.each(list1, (node) => {
      list.push(node)
    })
    _.each(list2, (node) => {
      const i = _.findIndex(list, (n)=>{return n.id === node.id})
      if(i>=0) {
        list[i] = Immutable.merge(list[i], {children: this.mergeChildren(list[i].children, node.children)})
      } else {
        list.push(node)
      }
    })
    return _.sortBy(list, ['id'])
  }

  updateAllVisibility = () => { // TODO rename to init visibility
    // _.each(this.state.hierarchy, (facetInMap) => {
    //   this.updateNodeVisibility(facetInMap, this.lookupFacet(facetInMap.id).open)
    //   // this.updateNodeVisibility(facetInMap, true) // init state of open for all is: no
    //   let node = this.lookupFacet(facetInMap.id)
    //   this.replaceNode(facetInMap.id, Immutable.merge(node, {visible: true})) // TODO this is a dumb method to update one property on the node. Make it like updateNode(id, propName, value)??
    // }) // TODO this really should recurse, because new nodes can appear at weird nested places...


    this.setState(prevState => {
      const facets = prevState.facetList
      _.each(this.state.hierarchy, (facetInMap) => {
        const index = this.facetIndex(facetInMap.id) // TODO this is copy pasted from updateNodeVisibility - make a common function
        facets[index] = Immutable.merge(facets[index], {visible: true})

        // let updateVisibility = (children, visibility) => {
        //   _.each(children, (value, key) => {
        //     const i = this.facetIndex(value.id)
        //     facets[i] = Immutable.merge(facets[i], {visible: visibility})
        //     updateVisibility(value.children, visibility && value.open)
        //   })
        // }
        // updateVisibility(facetInMap.children, facets[index].open)

        this.mutateFacetVisibility(facetInMap.children, facets, index)
      })

      console.log('update all vis:', facets)
      return {
        ...prevState,
        facetList: facets,
      }
    })
  }

  componentWillReceiveProps(nextProps) {
    if(!_.isEqual(this.props.hierarchy, nextProps.hierarchy)) {
    // TODO need a way to determine when the hierarchy really should change... (ie reset entirely if map is empty??)
      if(_.isEmpty(nextProps.hierarchy)) {console.log('TODO RESET HIERARCY')} // TODO not triggered when I hoped it would be...
    }

    if(!_.isEqual(this.props.hierarchy, nextProps.hierarchy) || !_.isEqual(this.props.facetMap, nextProps.facetMap)) {
      this.setState(prevState => {
        const facets = _.map(this.state.facetList, (facet) => {
          let f = Immutable.merge(facet, {count: 0}) // set all facet counts to zero
          const updatedFacet = _.find(nextProps.facetMap, (updatedFacet)=> { return facet.id === updatedFacet.id})
          if(updatedFacet) {
            // update facet with refreshed info (ie count) from results
            f = Immutable.merge(f, {count: updatedFacet.count, selected: updatedFacet.selected})
            // TODO can probably just do f = Immutable.merge(f, updatedFacet), since updatedFacet shouldn't have any UI state in it.
          }// else {console.log('stuck at count 0')}

          return f
        })

        // find any new facets which are not already included and add them
        _.each(nextProps.facetMap, (facet) => {
          const oldFacet = _.find(this.state.facetList, (oldFacet)=> { return facet.id === oldFacet.id})
          if(!oldFacet) {
            facets.push(Immutable.merge({
              open: false,
              visible: false,
              tabIndex: '-1', // TODO why is this a string and not an int? or why is the zero below an int?? (parital answer - Facet has a logic check that assumes a string, but ... ummm?)
            },facet))
          }
        })

        return {
          ...prevState,
          facetList: _.sortBy(facets, ['term']), // make sure facets are sorted
          hierarchy: this.mergeChildren(this.state.hierarchy, nextProps.hierarchy)
        }
      }, this.updateAllVisibility)
    }
  }

  componentDidMount() {
    // init state
    this.setState(prevState => {
      const facets = _.map(this.props.facetMap, (facet)=> {
        return Immutable.merge(facet, {
          open: false,
          visible: false,
          tabIndex: '-1',
        })
      })
      // The first facet should be the only focusable one, initially.
      facets[0] = Immutable.merge(facets[0], {tabIndex: '0'})
      const firstFocused = facets[0]

      return {
        ...prevState,
        facetList: facets,
        hierarchy: this.props.hierarchy,
        rovingIndex: firstFocused.id,
      }
    }, this.updateAllVisibility)
  }

  lookupFacet = (id) => {
    return _.find(this.state.facetList, (facet) => {
      return facet.id === id
    })
  }

  facetIndex = (id) => {
    return _.findIndex(this.state.facetList, (facet) => {
      return facet.id === id
    })
  }

  replaceNode = (id, newNode) => { // TODO could be changed to update node by id?
    const index = _.findIndex(this.state.facetList, (facet) => {
      return facet.id === id
    })
    this.setState(prevState => {
      const facets = this.state.facetList
      facets[index] = newNode
      return {
        ...prevState,
        facetList: facets,
      }
    })
  }

  mutateFacetVisibility = (hierarchy, list, index) => {
    console.log('updating visibility for', hierarchy, index, list)
    let updateVisibility = (children, visibility) => {
      _.each(children, (value, key) => {
        const i = this.facetIndex(value.id)
        console.log('vis vis', visibility, list[i])
        list[i] = Immutable.merge(list[i], {visible: visibility})
        console.log('vis subvis', visibility, value.open, visibility && value.open)
        if(value.children.length > 0 && value.open == null) {
          console.log('what in the good heck?', value.id, value.open, value.children)
        }
        updateVisibility(value.children, visibility && list[i].open)
      })
    }
    console.log('wtf', list[index])
    updateVisibility(hierarchy, list[index].open)
  }

  updateNodeVisibility = (facetInMap, open) => {
    this.setState(prevState => {
      const facets = prevState.facetList
      const index = this.facetIndex(facetInMap.id)
      facets[index] = Immutable.merge(facets[index], {open: open})

      // let updateVisibility = (children, visibility) => {
      //   _.each(children, (value, key) => {
      //     const i = this.facetIndex(value.id)
      //     facets[i] = Immutable.merge(facets[i], {visible: visibility})
      //     updateVisibility(value.children, visibility && value.open)
      //   })
      // }
      // updateVisibility(facetInMap.children, facets[index].open)
      this.mutateFacetVisibility(facetInMap.children, facets, index)

      return {
        ...prevState,
        facetList: facets,
      }
    })
  }

  handleExpandableToggle = event => {
    this.updateNodeVisibility(event.value, event.open)
  }

  updateRovingIndex = facetId => {
    this.setState(prevState => {
      const oldIndex = this.facetIndex(this.state.rovingIndex)
      const newIndex = this.facetIndex(facetId)
      const facets = this.state.facetList
      facets[oldIndex] = Immutable.merge(facets[oldIndex], {tabIndex: '-1'})
      facets[newIndex] = Immutable.merge(facets[newIndex], {tabIndex: '0'})
      return {
        ...prevState,
        facetList: facets,
        rovingIndex: facetId,
      }
    })

    document.getElementById(facetId).focus()
  }

  lookupHierarchy = (id, list) => {
    let result = null
    _.each(list, (node) => {
      if(result) return
      if(node.id === id) {
        result = node
        return
      }
      result = this.lookupHierarchy(id, node.children)
    })
    return result
  }

  triggerRight = () => {
    const id = this.state.rovingIndex
    const node = this.lookupFacet(id)
    const nodeInMap = this.lookupHierarchy(id, this.state.hierarchy)
    if (!node.open) {
      // open node
      this.handleExpandableToggle({open: true, value: nodeInMap})
    }
    else {
      // move focus to first child
      const facet = nodeInMap.children[0]
      if (facet) {
        this.updateRovingIndex(facet.id)
      }
    }
  }

  triggerLeft = () => {
    const id = this.state.rovingIndex
    const node = this.lookupFacet(id)
    const nodeInMap = this.lookupHierarchy(id, this.state.hierarchy)
    if (node.open) {
      // close node
      this.handleExpandableToggle({open: false, value: nodeInMap})
    }
    else {
      // move focus to parent
      const facetId = nodeInMap.parent
      if (facetId) {
        this.updateRovingIndex(facetId)
      }
    }
  }

  moveFocusDown = () => {
    console.log('down down down', this.state.facetList)
    const id = this.state.rovingIndex
    const orderIndex = this.facetIndex(id)

    if (orderIndex < this.state.facetList.length - 1) {
      const nextVisible = _.find(
        this.state.facetList,
        facet => {
          return facet.visible
        },
        orderIndex + 1
      )

      if (nextVisible) {
        this.updateRovingIndex(nextVisible.id)
      }
    }
  }

  moveFocusToEnd = () => {
    const nextVisible = _.findLast(this.state.facetList, facet => {
      return facet.visible
    })

    if (nextVisible) {
      this.updateRovingIndex(nextVisible.id)
    }
  }

  moveFocusToStart = () => {
    this.updateRovingIndex(this.state.facetList[0].id)
  }

  moveFocusUp = () => {
    const id = this.state.rovingIndex
    const orderIndex = this.facetIndex(id)

    if (orderIndex > 0) {
      const nextVisible = _.findLast(
        this.state.facetList,
        facet => {
          return facet.visible
        },
        orderIndex - 1
      )

      this.updateRovingIndex(nextVisible.id)
    }
  }

  handleSelectToggleMouse = e => {
    this.props.handleSelectToggle(e.value, e.checked)
  }

  createFacetComponent = (facetInMap) => {
    const facet = this.lookupFacet(facetInMap.id)
    let facetComponent = null
    if (!facet) {
      return facetComponent
    }
    const facetChildren = _.map(facetInMap.children, facet => this.createFacetComponent(facet))
    const hasChildren = !_.isEmpty(facetInMap.children)
    const children = hasChildren
      ? facetChildren
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
        disabled={facet.count == 0}
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
    return facetChildren
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
    const facetHierarchy = _.map(this.state.hierarchy, (facet) => {return this.createFacetComponent(facet)})

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
