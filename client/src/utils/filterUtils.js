import _ from 'lodash'
import Immutable from 'seamless-immutable'

// export const toggleSelectedId = (selectedCollectionIds, value, idx = 0) => {
//   // base case: reached the end of the selectedCollectionIds list without encountering the value, so we add (select) it
//   if (idx === selectedCollectionIds.length) {
//     return selectedCollectionIds.concat([ value ])
//   }
//   else if (selectedCollectionIds[idx] === value) {
//     // found an already selected ID matching the value, so we can remove (unselect) it
//     return selectedCollectionIds.slice(0, idx).concat(selectedCollectionIds.slice(idx + 1))
//   }
//   else {
//     // recurse through selectedCollectionIds until base case is met
//     return toggleSelectedId(selectedCollectionIds, value, idx + 1)
//   }
// }

export const updateSelectedFacets = (
  selectedFacets,
  category,
  term,
  selected
) => {
  const selectedTerms = selectedFacets[category]

  // add to selected facets, if needed
  if (selected) {
    if (!selectedTerms) {
      // both category and term aren't yet in the selectedTerms
      return Immutable.set(selectedFacets, category, [ term ])
    }
    else if (!selectedTerms.includes(term)) {
      // the term isn't yet in the selectedTerms
      return Immutable.set(
        selectedFacets,
        category,
        selectedTerms.concat([ term ])
      )
    }
    else {
      // already selected, no need to duplicate term
      return selectedFacets
    }
  }
  else {
    // remove from selected facets, if needed
    if (!selectedTerms) {
      // can't remove if category doesn't exist in selectedFacets
      return selectedFacets
    }
    else {
      // search for index of term to be removed from selectedFacets
      let removeIndex = selectedTerms.indexOf(term)
      // the term exists to be removed
      if (removeIndex > -1) {
        const beforeRemove = selectedTerms.slice(0, removeIndex)
        const afterRemove = selectedTerms.slice(removeIndex + 1)
        const newTerms = beforeRemove.concat(afterRemove)

        // remove the whole category from selectedFacets if the new terms array is empty
        if (_.isEmpty(newTerms)) {
          return Immutable.without(selectedFacets, category)
        }
        else {
          // otherwise replace the category terms array with the newTerms
          return Immutable.set(selectedFacets, category, newTerms)
        }
      }
      else {
        // the term does not exist to be removed
        return selectedFacets
      }
    }
  }
}
