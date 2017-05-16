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
    'react-hot-loader/patch',
    // activate HMR for React

    'webpack-dev-server/client?http://localhost:8080',
    // bundle the client for webpack-dev-server
    // and connect to the provided endpoint

    'webpack/hot/only-dev-server',
    // bundle the client for hot reloading
    // hot reload for successful updates
    './index.jsx'
  ],
  output: {
    path: path.resolve(__dirname, 'dist'),
    publicPath: './',
    filename: '[name].bundle.js'
  },
  context: path.resolve(__dirname, 'src'),
  devtool: 'cheap-module-eval-source-map',
  devServer: {
    publicPath: '/onestop/',
    contentBase: path.resolve(__dirname, 'dist'),
    hot: true,
    proxy: {
      '/onestop/api/*': {
        target: 'http://localhost:8097/',
        secure: false
      }
    }
  },
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
        loader: 'style-loader',
        options: {
          sourceMap: true
        }
      }, {
        loader: 'css-loader'
      }]
    }, {
      test: /\.css$/,
      exclude: /node_modules/,
      use: [{
        loader: 'style-loader',
        options: {
          sourceMap: true
        }
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
    unsafeCache: true
  },
  plugins: [
    new HtmlWebpackPlugin({
      title: 'NOAA OneStop',
      favicon: '../img/noaa-favicon.ico'
    }),
    new webpack.optimize.CommonsChunkPlugin({
      name: 'vendor',
      minChunks: function (module) {
        return module.context && module.context.indexOf('node_modules') !== -1
      }
    }),
    //new webpack.optimize.CommonsChunkPlugin("vendor", "vendor-bundle-[hash].js")
    new webpack.HotModuleReplacementPlugin(),
    // enable HMR globally

    new webpack.NamedModulesPlugin()
    // prints more readable module names in the browser console on HMR updates
  ]
}
