
var decimal = require('decimal');

var a = "1234";
var fs = 2;

var z = decimal();
console.dir(z.toString());

var d = decimal(a);
var d2 = d.div(Math.pow(10, fs));

console.dir(d2.toString());
