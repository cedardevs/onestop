const express = require('express')
var fs = require('fs')
const generateSitemap = require('./generator')
var dir = './public';

const app = express()

if(!fs.existsSync(dir)){
  fs.mkdirSync(dir);
}

generateSitemap();

app.use(express.static(dir))


app.listen(3000, () => {
  console.log('listening')
});