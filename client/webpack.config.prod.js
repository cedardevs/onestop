const webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const precss = require('precss')
const autoprefixer = require('autoprefixer')
const postcssAssets = require('postcss-assets')
const path = require('path')
require('babel-polyfill')
const modernizrrc = path.resolve(__dirname, '.modernizrrc.json')
require(modernizrrc)

module.exports = {
  entry: [
    'babel-polyfill',
    modernizrrc,
    './index.jsx'
  ],
  output: {
    path: path.resolve(__dirname, 'dist'),
    publicPath: './',
    filename: '[name]-[hash].bundle.js'
  },
  context: path.resolve(__dirname, 'src'),
  devtool: false,
  module: {
    rules: [{
      test: /\.modernizrrc.json/,
      use: [ 'modernizr-loader', 'json-loader']
    }, {
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
            [ 'env', { modules: false } ],
            'react', 'stage-0'
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
        'file-loader?hash=sha512&digest=hex&name=[name]-[hash].[ext]',
        'image-webpack-loader?bypassOnDebug&optimizationLevel=7&interlaced=false'
      ],
    }, {
      test: /\.(ttf|otf|eot|svg|woff(2)?)(\?[a-z0-9]+)?$/,
      use: [
          'file-loader?name=fonts/[name]-[hash].[ext]'
      ]
    }]
  },
  resolve: {
    modules: [path.resolve('./node_modules/leaflet/dist', 'root'), 'node_modules',
      path.resolve('./src/common/link')],
    extensions: ['.js', '.jsx'],
    alias: {
      'fa': path.resolve(__dirname, 'img/font-awesome/white/svg/'),
      modernizr$: path.resolve(__dirname, ".modernizrrc.json")
    }
  },
  plugins: [
    new HtmlWebpackPlugin({
      inject: false,
      template: require('html-webpack-template'),
      title: 'NOAA OneStop',
      favicon: '../img/noaa-favicon.ico',
      lang: 'en-US'
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
      minChunks(module, count) {
        var context = module.context
        return context && context.includes('node_modules')
      }
    })
  ]
}
