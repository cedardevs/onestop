var webpack = require('webpack')
var HtmlWebpackPlugin = require('html-webpack-plugin')
var precss = require('precss')
var autoprefixer = require('autoprefixer')

module.exports = {
  devtool: 'source-map',
  entry: {
    app: './src/index.jsx'
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
  devtool: 'source-map',
  devServer: {
    contentBase: './dist',
    hot: true
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new webpack.optimize.UglifyJsPlugin(),
    new HtmlWebpackPlugin({
      title: 'NOAA OneStop'
    })
  ],
  node: {
    fs: "empty"
  }
};
