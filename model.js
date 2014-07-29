var mongo = require('mongodb');
var async = require('async');
var decimal = require('decimal');
var extend = require('extend');

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
    update: function(id, data, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.update({_id: id}, data, {w:1}, callback);
            }
        });
    },
    getBalance: function(id, callback) {
        //console.log("computing page balance for "+id);
        async.parallel({
            total_income: function(next) {
                var total = decimal('0');
                //console.log("looking for income unage page id " + id);
                exports.Income.findByPageID(id, function(err, incomes) {
                    async.forEach(incomes, function(income, next_income) {
                        if(income.balance_from) {
                            //recurse (for now)
                            //console.dir(income);
                            //console.log("in-calling getBlanace for "+income.balance_from);
                            exports.Page.getBalance(income.balance_from, function(amount) {
                                total = total.add(amount);
                                next_income();
                            });
                        } else {
                            //console.log(total);
                            //console.dir(income);
                            total = total.add(income.amount);
                            next_income();
                        }
                    }, function() {
                        //console.log("total income was "+total);
                        next(null, total);
                    });
                });
            },
            total_expense: function(next) {
                var total = decimal('0');
                exports.Category.findByPageID(id, function(err, categories) {
                    categories.forEach(function(category) {
                        category.expenses.forEach(function(expense) {
                            if(!expense.tentative) {
                                total = total.add(expense.amount);
                                //console.log("adding " + expense.amount);
                                //console.log("total " + total);
                            }
                        });
                    });
                    next(null, total);
                });
            }
        }, function(err, ret){
            var balance = ret.total_income.sub(ret.total_expense);
            //console.log("balance:"+balance);
            callback(balance.toString());
        });
    },
    remove: function(id, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.remove({_id: id}, {w:1}, callback);
            }
        });
    }
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
                col.update({_id: id}, data, {w:1}, callback);
            }
        });
    }
};

exports.Income = {
    findByID: function(id, callback) {
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({_id: id}, {}, callback);
            }
        });
    },
    findByPageID: function(id, callback) {
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                col.find({page_id:id}).toArray(callback);
                /*
                col.find({page_id:id}).toArray(function(err, incomes) {
                    //set amount for balance incomes
                    //console.dir(incomes);
                    async.forEach(incomes, function(income, next) {
                        if(income.balance_from) {
                            //load page name
                            exports.Page.findByID(income.balance_from, function(err, page) {
                                income.page_name = page.name;
                                //load balance amount
                                console.log("requesting getBlance while loading income for "+page.name+" ("+page._id+")");
                                exports.Page.getBalance(income.balance_from, function(amount) {
                                    //console.log("balance: "+amount);
                                    income.amount = amount;
                                    next();
                                });
                            });
                        } else {
                            next();
                        }
                    }, function() {
                        //console.dir(incomes);
                        callback(err, incomes);
                    });
                });
                */
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
                col.update({_id: id}, data, {w:1}, callback);
            }
        });
    },
    remove: function(id, callback) {
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                col.remove({_id: id}, {w:1}, callback);
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
                col.update({_id: id}, data, {w:1}, callback);
            }
        });
    },
    remove: function(id, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                col.remove({_id: id}, {w:1}, callback);
            }
        });
    }
};

