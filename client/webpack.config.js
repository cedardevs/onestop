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
      'react-tap-event-plugin']
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
      loader: 'react-hot!babel'
    }, {
      test: /\.css$/,
      loaders: [
        'style?sourceMap',
        'css?modules&importLoaders=1&localIdentName=[name]__[local]___[hash:base64:5]',
        'postcss']
    }, {
      test: /\.(png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$/,
      loader: 'file' + '?name=[path][name].[ext]'
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
    publicPath: '/',
    filename: 'bundle-[hash].js'
  },
  devtool: 'eval-cheap-module-source-map',
  devServer: {
    contentBase: './dist',
    hot: true,
    proxy: {
      '/api/*': {
        target: 'http://localhost:8097/',
        secure: false
      }
    }
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': process.env.NODE_ENV
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      compress: {warnings: false},
      sourceMap: false
    }),
    new HtmlWebpackPlugin({
      title: 'NOAA OneStop'
    }),
    new webpack.optimize.CommonsChunkPlugin("vendor", "vendor-bundle-[hash].js")
  ]
}
