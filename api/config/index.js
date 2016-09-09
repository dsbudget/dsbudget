'use strict';

const fs = require('fs');
const winston = require('winston');

exports.dsbudget = {
    test: "hello"
}

exports.mongodb = "mongodb://localhost/dsbudget";

exports.express = {
    port: 8080,
}

exports.logger = {
    winston: {
        //hide headers which may contain jwt
        requestWhitelist: ['url', /*'headers',*/ 'method', 'httpVersion', 'originalUrl', 'query'],
        transports: [
            //display all logs to console
            new winston.transports.Console({
                timestamp: function() {
                    var d = new Date();
                    return d.toString(); 
                },
                level: 'debug',
                colorize: true
            }),
        ]
    },
}

