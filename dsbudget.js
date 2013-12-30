/*
var Db = require('mongodb').Db;
var Connection = require('mongodb').Connection;
var Server = require('mongodb').Server;
var BSON = require('mongodb').BSON;
var ObjectID = require('mongodb').ObjectID;
var assert = require('assert')
*/



/*
var config = require('./config.json');
function checkAuth(req, res, next) {
    if (!req.session.user_id) {
        res.send('You are not authorized to view this page');
    } else {
        next();
    }
}
*/

exports.PageModel = function(db) {
    this.db = db;
};

/*
PageModel.prototype.getDB = function(callback) {
    if(this.db_connected) callback(this.db);
    else {
        console.log('connecting to mongodb');
        var that = this;
        this.db.open(function(err, db){
            assert.equal(null, err);
            that.db_connected = true;
            db.authenticate(config.mongo_user, config.mongo_pass, function(err, result) {
                console.log("authenticated");
                callback(db);
            });
        });
    }
};
*/

exports.PageModel.prototype.findAll = function(callback) {
    this.db.collection('page', function(err, page_collection) {
        if(err) callback(err)
        else {
            page_collection.find().toArray(callback);
        }
    });
};

/*
PageModel.prototype.findById = function(id, callback) {
    this.getCollection(function(error, page_collection) {
      if( error ) callback(error)
      else {
        //var ObjectID = mongo.ObjectID;
        page_collection.findOne({_id: page_collection.db.bson_serializer.ObjectID.createFromHexString(id)}, function(error, result) {
        debugger;
          if( error ) callback(error)
          else callback(null, result)
        });
      }
    });
};

PageModel.prototype.save = function(pages, callback) {
    this.getCollection(function(error, page_collection) {
      if( error ) callback(error)
      else {
        if( typeof(pages.length)=="undefined")
          pages = [pages];

        for( var i =0;i< pages.length;i++ ) {
          page = pages[i];
          page.created_at = new Date();
          if( page.comments === undefined ) page.comments = [];
          for(var j =0;j< page.comments.length; j++) {
            page.comments[j].created_at = new Date();
          }
        }

        console.log("inserting now");
        console.dir(pages);
        page_collection.insert(pages, function() {
            console.log("inserted");
          callback(null, pages);
        });
      }
    });
};
*/

