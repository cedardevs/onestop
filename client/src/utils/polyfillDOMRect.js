/*
This version of DOMRect polyfill came from https://github.com/jarek-foksa/geometry-polyfill/blob/master/geometry-polyfill.js

For some reason, importing geometry-polyfill was giving us problems in IE11 due to other classes we aren't using in this project.
*/
{
  class DOMRect {
    constructor(x = 0, y = 0, width = 0, height = 0) {
      this.x = x
      this.y = y
      this.width = width
      this.height = height
    }

    static fromRect(otherRect) {
      return new DOMRect(
        otherRect.x,
        otherRect.y,
        otherRect.width,
        otherRect.height
      )
    }

    get top() {
      return this.y
    }

    get left() {
      return this.x
    }

    get right() {
      return this.x + this.width
    }

    get bottom() {
      return this.y + this.height
    }
  }

  window.DOMRect = window.DOMRect || DOMRect
}
