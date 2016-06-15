var webpack = require('webpack')
var HtmlWebpackPlugin = require('html-webpack-plugin')
var precss = require('precss')
var autoprefixer = require('autoprefixer')

module.exports = {
  entry: {
    app: './src/index.jsx',
    vendor: ['purecss', 'react', 'react-dom', 'react-router', 'redux', 'redux-thunk',
      'react-tap-event-plugin']
  },
  module: {
    preLoaders: [{
      test: /\.js$/,
      loader: "eslint-loader",
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
      test: /\.(jpe?g|png|gif|svg)$/,
        loaders: [
            'file?hash=sha512&digest=hex&name=[hash].[ext]',
            'image-webpack?bypassOnDebug&optimizationLevel=7&interlaced=false'
        ]
    }]
  },
  postcss: function(){
    return [precss, autoprefixer]
  },
  resolve: {
    extensions: ['', '.js', '.jsx']
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
    new webpack.HotModuleReplacementPlugin(),
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
