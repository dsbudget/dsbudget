var fs = require('fs');
var xml2js = require('xml2js');
var async = require('async');
var extend = require('extend');

function parse_page(model, doc_id, opts, page, next_page) {
    //skip "New Page
    if(page.$.ctime == 0) {
        next_page();
        return;
    }

    function parse_amount(amount) {
        var a = parseInt(amount);
        a /= Math.pow(10, opts.fd);
        return a;
    }

    var db_page = {
        doc_id: doc_id,
        name: page.$.name,
        desc: page.$.description,
        start_date: page.$.ctime*1000,
        end_date: page.$.ctime*1000 + 3600*24*40, //TODO - set it to end of month? 
        total_income: 1000, //will be reset later
        total_expense: 1000, // will be reset later
        show_views: {
            balance: (page.$.hide_balance == "yes" ? true : false),
            budget: (page.$.hide_budget == "yes" ? true : false),
            expense: (page.$.hide_expense == "yes" ? true : false),
            income: (page.$.hide_income == "yes" ? true : false)
        }
    };

    model.Page.create(db_page, function(err, page_id) {

        function parse_deductions(name, deductions, next) {
            var db_deduction_category = {
                page_id: page_id,
                name: name, 
                desc: "",
                budget: 0,
                is_amount_per: false,
                color: "#ffe",
                show_view: {balance_graph: false, pie_graph: false},
                sort_by: "name",
                sort_asc: true,
                recurring: true,
                expenses: [],
            };
            async.forEach(deductions, function(deduction, next_deduction) {
                var db_expense = {
                    name: deduction.$.desc,
                    amount: parse_amount(deduction.$.amount),
                    is_amount_per: false,
                    where: "(Deduction)",
                    time: page.$.ctime, //use page's ctime as expense time
                };
                db_deduction_category.budget += db_expense.amount;
                db_deduction_category.expenses.push(db_expense);
                next_deduction();
            }, function() {
                model.Category.create(db_deduction_category, function(err, category_id) {
                    if(err) throw err;
                    next();
                });
            });
        }

        function parse_income(income, next) {
            var db_income = {
                page_id: page_id,
                is_balance: (income.$.balance == "yes" ? true : false),
                amount: parse_amount(income.$.amount),
                name: income.$.desc
            };
            //TODO - need to process linking is is_balance is true
            model.Income.create(db_income, function(err, income_id) {
                if(err) throw err;
                //console.dir(db_income);
                //console.dir(income);
                if(income.Deduction) {
                    parse_deductions("Deductions for "+income.$.desc, income.Deduction, next);
                } else {
                    next();
                }
            });
        }

        function parse_color(v) {
            var v = parseInt(v);
            var r = (v>>0)&0xff;
            var g = (v>>8)&0xff;
            var b = (v>>16)&0xff;
            return "#"+r.toString(16)+g.toString(16)+b.toString(16);
        }

        function insert_category(db_category, next) {
        }

        function parse_category(category, next) {
            var non_recurring_expenses = [];
            var recurring_expenses = [];
            var db_category = {
                page_id: page_id,
                name: category.$.name, 
                desc: category.$.desc,
                budget: parse_amount(category.$.budget),
                is_amount_per: (category.$.amount_is_percentage == "true" ? true : false),
                color: parse_color(category.$.color),
                show_view: {
                    balance_graph: (category.$.hide_balance_graph == "yes" ? false : true),
                    pie_graph: (category.$.hide_pie_graph == "yes" ? false : true)
                },
                sort_by: category.$.sort_by, //TODO - I need to map to the real field name
                sort_asc: (category.$.sort_reverse == "yes" ? false : true),
                recurring: false
            };

            if(category.Spent) {
                async.forEach(category.Spent, function(spent, next_spent) {
                    var recurring = (spent.$.recurring == "yes" ? true : false);
                    var expense = {
                        name: spent.$.desc,
                        amount: parse_amount(spent.$.amount),
                        where: spent.$.where,
                        time: parseInt(spent.$.time)*1000,
                        tentative: (spent.$.tentative == "yes" ? true : false)
                    };
                    if(recurring) {
                        recurring_expenses.push(expense);
                    } else {
                        non_recurring_expenses.push(expense);
                    }
                    next_spent();
                }, function() {
                    async.series([
                        function(next_type) {
                            if(recurring_expenses.length > 0) {
                                var db_cat = extend({}, db_category, {
                                    recurring: true,
                                    expenses: recurring_expenses
                                });
                                console.dir(db_cat);
                                model.Category.create(db_cat, function(err, category_id) {
                                    if(err) throw err;
                                    next_type();
                                });
                            } else {
                                next_type();
                            }
                        },
                        function(next_type) {
                            if(non_recurring_expenses.length > 0) {
                                var db_cat = extend({}, db_category, {
                                    recurring: false,
                                    expenses: non_recurring_expenses
                                });
                                model.Category.create(db_cat, function(err, category_id) {
                                    if(err) throw err;
                                    next_type();
                                });
                            } else {
                                next_type();
                            }
                        },
                    ], function() {
                        next();
                    });
                });
            } else {
                //insert empty category
                model.Category.create(db_category, function(err, category_id) {
                    if(err) throw err;
                    next();
                });
            }
        }

        async.series([
            function(next) {
                var incomes = page.Income;
                if(incomes) {
                    async.forEach(incomes, parse_income, function() {
                        next(null, 'incomes'); 
                    });
                } else {
                    console.log(db_page.name + " doesn't have any income");
                    next(null, 'no_incomes');
                }
            },
            function(next) {
                var categories = page.Category;
                if(categories) {
                    async.forEach(categories, parse_category, function() {
                        console.log("going to next category"); 
                        next(null, 'categories'); 
                    });
                } else {
                    console.log(db_page.name + " doesn't have any categories");
                    next(null, 'no_categoryes');
                }
            }
        ], next_page);
    });
}



exports.dsbudget = function(model, docid, path, opts, callback) {
    fs.readFile(path, function(err, data) {
        if(err) {
            callback(err);
        } else {
            var parser = new xml2js.Parser();
            parser.parseString(data, function(err, doc) {
                if(err) {
                    callback(err);
                } else {
                    var docversion = doc.Budget.$.docversion;
                    var openpage = doc.Budget.$.openpage;
                    var pages = doc.Budget.Page;
                    async.forEach(pages, function(page, next) {
                        parse_page(model, docid, opts, page, next);
                    }, callback);
                }
            });
        }
    });
};
