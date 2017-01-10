var webpack = require('webpack')
var HtmlWebpackPlugin = require('html-webpack-plugin')
var precss = require('precss')
var autoprefixer = require('autoprefixer')
var postcssAssets = require('postcss-assets')
var path = require('path')


module.exports = {
  entry: {
    app: './src/index.jsx',
    vendor: ['purecss', 'react', 'react-dom', 'react-router', 'redux', 'redux-thunk',
      'leaflet']
  },
  module: {
    preLoaders: [{
      test: /\.js$/,
      loader: "eslint",
      exclude: /node_modules/
    }],
    loaders: [{
      test: /\.jsx?$/,
      exclude: /node_modules/,
      loader: 'babel'
    }, {
      test: /\.css$/,
      include: /node_modules/,
      loaders: [
        'style?sourceMap',
        'css'
      ]
    }, {
      test: /\.css$/,
      exclude: /node_modules/,
      loaders: [
        'style?sourceMap',
        'css?modules&importLoaders=1&localIdentName=[name]__[local]___[hash:base64:5]',
        'postcss'
      ]
    }, {
      test: /\.(jpe?g|png|gif)$/,
      loaders: [
        'file?hash=sha512&digest=hex&name=[hash].[ext]',
        'image-webpack?bypassOnDebug&optimizationLevel=7&interlaced=false'
      ],
    }, {
      test: /\.(svg)(\?v=\d+\.\d+\.\d+)?$/,
      loader: 'url?limit=65000&mimetype=image/svg+xml&name=public/fonts/[name].[ext]'
    }, {
        test: /\.(woff)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url?limit=65000&mimetype=application/font-woff&name=public/fonts/[name].[ext]'
    }, {
        test: /\.(woff2)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url?limit=65000&mimetype=application/font-woff2&name=public/fonts/[name].[ext]'
    }, {
        test: /\.([ot]tf)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url?limit=65000&mimetype=application/octet-stream&name=public/fonts/[name].[ext]'
    }, {
        test: /\.(eot)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url?limit=65000&mimetype=application/vnd.ms-fontobject&name=public/fonts/[name].[ext]'
    }]
  },
  postcss: function(){
    return [postcssAssets({
        loadPaths: ['**']
      }), precss, autoprefixer]
  },
  resolve: {
    extensions: ['', '.js', '.jsx'],
    root: [
      path.resolve('./node_modules/leaflet/dist')
    ]
  },
  output: {
    path: __dirname + '/dist',
    publicPath: './',
    filename: 'bundle-[hash].js'
  },
  devtool: '#eval-source-map',
  debug: true,
  devServer: {
    publicPath: '/onestop/',
    contentBase: './dist',
    hot: true,
    proxy: {
      '/onestop/api/*': {
        target: 'http://localhost:8097/',
        secure: false
      }
    }
  },
  plugins: [
    // TODO - This is not working in the running app right now. Can fix later as needed.
    // new webpack.DefinePlugin({
    //   'process.env': {
    //     'NODE_ENV': process && process.env && process.env.NODE_ENV || 'development'
    //   }
    // }),
    new webpack.optimize.UglifyJsPlugin({
      compress: {warnings: false},
      sourceMap: false
    }),
    new HtmlWebpackPlugin({
      title: 'NOAA OneStop Demo',
      favicon: './img/noaa-favicon.ico'
    }),
    new webpack.optimize.CommonsChunkPlugin("vendor", "vendor-bundle-[hash].js")
  ]
}
