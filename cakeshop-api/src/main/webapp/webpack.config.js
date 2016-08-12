var path = require('path');
var webpack = require('webpack');

module.exports = {
  entry: "./js/index.js",
  output: {
    path: path.join(__dirname, 'js'),
    filename: "index-gen.js",
  },
  plugins: [
    new webpack.ProvidePlugin({
      $: "jquery",
      jQuery: "jquery",
      "window.jQuery": "jquery",
      d3: "d3",
      Backbone: "backbone",
      _: "underscore",
      SockJS: "sockjs-client",
      moment: "moment",
    }),
  ],
  module: {
    loaders: [
      {
        test: /\.js$/,
        exclude: /(node_modules)/,
        loader: 'babel',
        query: {
          presets: ['es2015']
        }
      }
    ]
  }
};
