const webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const precss = require('precss')
const autoprefixer = require('autoprefixer')
const postcssAssets = require('postcss-assets')
const path = require('path')
require('babel-polyfill')

module.exports = {
  entry: [
    'babel-polyfill',
    './index.jsx'
  ],
  output: {
    path: path.resolve(__dirname, 'dist'),
    publicPath: './',
    filename: '[name].bundle.js'
  },
  context: path.resolve(__dirname, 'src'),
  devtool: false,
  module: {
    rules: [{
      enforce: 'pre',
      test: /\.js$/,
      use: 'eslint-loader',
      exclude: /node_modules/
    }, {
      test: /\.jsx?$/,
      exclude: /node_modules/,
      use: {
        loader: 'babel-loader',
        options: {
          presets: [
            [ 'es2015', { modules: false } ]
          ]
        }
      }
    }, {
      test: /\.css$/,
      include: /node_modules/,
      use: [{
        loader: 'style-loader'
      }, {
        loader: 'css-loader'
      }]
    }, {
      test: /\.css$/,
      exclude: /node_modules/,
      use: [{
        loader: 'style-loader'
      }, {
        loader: 'css-loader',
        options: {
          modules: true,
          importLoaders: true,
          localIdentName: '[name]__[local]___[hash:base64:5]',
          plugins: function () {
            return [
              require('precss'),
              require('autoprefixer')
            ]
          }
        }
      }, {
        loader: 'postcss-loader'
      }],
    }, {
      test: /\.(jpe?g|png|gif)$/,
      use: [
        'file-loader?hash=sha512&digest=hex&name=[hash].[ext]',
        'image-webpack-loader?bypassOnDebug&optimizationLevel=7&interlaced=false'
      ],
    }, {
      test: /\.(svg)(\?v=\d+\.\d+\.\d+)?$/,
      use: 'url-loader?limit=65000&mimetype=image/svg+xml&name=public/fonts/[name].[ext]'
    }, {
        test: /\.(woff)(\?v=\d+\.\d+\.\d+)?$/,
        use: 'url-loader?limit=65000&mimetype=application/font-woff&name=public/fonts/[name].[ext]'
    }, {
        test: /\.(woff2)(\?v=\d+\.\d+\.\d+)?$/,
        use: 'url-loader?limit=65000&mimetype=application/font-woff2&name=public/fonts/[name].[ext]'
    }, {
        test: /\.([ot]tf)(\?v=\d+\.\d+\.\d+)?$/,
        use: 'url-loader?limit=65000&mimetype=application/octet-stream&name=public/fonts/[name].[ext]'
    }, {
        test: /\.(eot)(\?v=\d+\.\d+\.\d+)?$/,
        use: 'url-loader?limit=65000&mimetype=application/vnd.ms-fontobject&name=public/fonts/[name].[ext]'
    }]
  },
  resolve: {
    modules: [path.resolve('./node_modules/leaflet/dist', 'root'), 'node_modules'],
    extensions: ['.js', '.jsx'],
  },
  plugins: [
    new HtmlWebpackPlugin({
      title: 'NOAA OneStop',
      favicon: '../img/noaa-favicon.ico'
    }),
    new webpack.DefinePlugin({
      'process.env':{
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      compress: {warnings: false}
    }),
    new webpack.LoaderOptionsPlugin({
      minimize: true,
      debug: false
    }),
    new webpack.optimize.CommonsChunkPlugin({
      name: 'vendor',
      filename: 'vendor.js',
      minChunks(module, count) {
        var context = module.context
        return context && context.includes('node_modules')
      }
    })
  ]
}
