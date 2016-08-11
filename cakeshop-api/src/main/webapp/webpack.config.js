var path = require('path');

module.exports = {
  entry: "./js/index.js",
  output: {
    path: path.join(__dirname, 'js'),
    filename: "index-gen.js",
  },
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
