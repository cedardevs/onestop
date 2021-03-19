require('dotenv').config({path:'./.env'})
const express = require('express')
const generateSitemap = require('./generator')
var CronJob = require('cron').CronJob;
var fs = require('fs')
var dir = './public';

console.log(__dirname);
const app = express()


//Creates folder to store sitemaps
if(!fs.existsSync(dir)){
  fs.mkdirSync(dir);
}


//generateSitemap();
console.log("Before job instantiation");
const job = new CronJob('0 */1 * * * *', function() {
	const d = new Date();
	console.log('Every 2 minutes:', d);
  generateSitemap();
});
console.log('After job instantiation');
job.start();

app.use(express.static(dir));

//Kubernetes health check
app.get('/', (req, res) => {
  res.send('Healthy!')
})

app.listen(3000, async () => {
  console.log('listening')
});
