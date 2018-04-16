module.exports = {
  plugins: [
    require('postcss-import')(
      {
        /* ...options */
      }
    ),
    require('precss')(
      {
        /* ...options */
      }
    ),
    require('autoprefixer')(
      {
        /* ...options */
      }
    ),
  ],
}
