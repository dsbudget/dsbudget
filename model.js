var mongo = require('mongodb');
var async = require('async');
var db;

exports.init = function(_db) {
    db = _db; 
    return exports;
}

exports.Doc = {
    findByOwnerID: function(id, callback) {
        db.collection('doc', function(err, col) {
            if(err) callback(err)
            else {
                col.find({owners: id}).toArray(callback);
            }
        });
    },
    getAuth: function(user, docid, callback) {
        var auth = {canread: false, canwrite: false, canadmin: false};
        exports.Doc.findByOwnerID(user._id, function(err, docs) {
            async.forEach(docs, function(doc, next) {
                if(doc._id.equals(docid)) {
                    auth.canread = true;
                    auth.canwrite = true;
                }
                next();
            }, function() {
                callback(null, auth);
            });
        }); 
    },
    create: function(doc, callback) {
        db.collection('doc', function(err, col) {
            if(err) callback(err)
            else {
                col.insert(doc, {safe:true}, function(err, recs) {
                    if(err) callback(err);
                    callback(null, recs[0]._id);
                });
            }
        });
    },
};

exports.Page = {
    findByID: function(id, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({_id:id}, {}, callback);
            }
        });
    },
    findByDocID: function(id, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.find({doc_id:id}).toArray(callback);
            }
        });
    },
    create: function(page, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                //console.log("inserting page");
                //console.dir(page);
                col.insert(page, {safe:true}, function(err, recs) {
                    if(err) callback(err);
                    callback(null, recs[0]._id);
                });
            }
        });
    },
};

exports.User = {
    findByID: function(id, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({_id: id}, {}, callback);
            }
        });
    },
    findByGoogleID: function(openid, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({googleid: openid}, {}, callback);
            }
        });
    },
    findByEmail: function(email, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({email: email}, {}, callback);
            }
        });
    },
    create: function(user, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                //console.log("inserting user");
                //console.dir(user);
                col.insert(user, {safe:true}, function(err, recs) {
                    if(err) callback(err);
                    console.dir(recs[0]._id);
                    callback(null, recs[0]._id);
                });
            }
        });
    },
    update: function(id, data, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                //console.log("updating: "+id);
                //console.dir(data);
                col.update({_id: id}, {$set: data}, {w:1}, callback);
            }
        });
    }
};

exports.Income = {
    findByPageID: function(id, callback) {
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                col.find({page_id:id}).toArray(callback);
            }
        });
    },
    create: function(income, callback) {
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                //console.log("inserting income");
                //console.dir(page);
                col.insert(income, {safe:true}, function(err, recs) {
                    if(err) callback(err);
                    callback(null, recs[0]._id);
                });
            }
        });
    },
    update: function(id, data, callback) {
        //console.log(id);
        //console.dir(data);
        delete data._id;
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                col.update({_id: id}, {$set: data}, {w:1}, callback);
            }
        });
    }
};

exports.Category = {
    findByID: function(id, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({_id: id}, {}, callback);
            }
        });
    },
    findByPageID: function(id, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                col.find({page_id:id}).toArray(callback);
            }
        });
    },
    create: function(category, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                col.insert(category, {safe:true}, function(err, recs) {
                    if(err) callback(err);
                    callback(null, recs[0]._id);
                });
            }
        });
    },
    update: function(id, data, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                //console.log("updating: "+id);
                //console.dir(data);
                delete data._id;
                col.update({_id: id}, {$set: data}, {w:1}, callback);
            }
        });
    }
};


