import _ from 'lodash'

const buildHierarchyMap = (category, terms) => {
  const createChildrenHierarchy = (map, hierarchy, term, value) => {
    // This function traverses down the hierarchy specified in the given map, creating empty 'children' objects ONLY if
    //   they don't already exist (otherwise just changes the reference). Since hierarchical strings are received tokenized
    //   and in alphabetical order (e.g.: 'Atmosphere', 'Atmosphere > Aerosols', 'Biosphere', etc.) this traversal down
    //   the nested object won't error out from one level to the next.
    const lastTerm = hierarchy.pop()
    if (!_.isEmpty(hierarchy)) {
      let i
      for (i = 0; i < hierarchy.length; i++) {
        map = map[hierarchy[i]].children = map[hierarchy[i]].children || {}
      }
    }

    map = map[lastTerm] = value
    return map
  }

  let categoryMap = {}

  Object.keys(terms).map(term => {
    let hierarchy = term.split('>').map( e => e.trim()) // Handling unfortunate instances of strings like "Spectral/Engineering >\t\t\t\t\t\t\tmicrowave"
    const value = {
      count: terms[term].count,
      children: {},
      category: category,
      term: term
    }
    createChildrenHierarchy(categoryMap, hierarchy, term, value)
  })

  return categoryMap
}

export const buildKeywordHierarchyMap = facetMap => {
  const hierarchyMap = {}
  _.map(facetMap, (terms, category) => {
    if (!_.isEmpty(terms)) { // Don't load categories that have no results
      let heading
      let categoryMap = {}

      if (category === 'science') {
        heading = 'Data Theme'
        categoryMap = buildHierarchyMap(category, terms)
      }
      else {
        heading = _.startCase(_.toLower((category.split(/(?=[A-Z])/).join(" "))))
        Object.keys(terms).map(term => {
          //const name = term.split(' > ')
          categoryMap[term] = {
            count: terms[term].count,
            children: {},
            category: category,
            term: term
          }
        })
      }

      hierarchyMap[heading] = categoryMap
    }
  })

  return hierarchyMap
}

// pulls out the last term in a GCMD-style keyword and attempts to maintain intended acronyms
export const titleCaseKeyword = term => {
  if (!term) { return null }
  // const trimmed = _.trim(term.split('>').pop(), '\\t')
  const trimmed = term.split('>').pop().trim()
  return (trimmed === trimmed.toUpperCase()) ? _.startCase(trimmed.toLowerCase()) : trimmed
}
