const express = require('express')
var fs = require('fs')
const generateSitemap = require('./generator')
var dir = './public';
var CronJob = require('cron').CronJob;

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

app.listen(3000, async () => {
  console.log('listening')
});
