db.dropDatabase();

db.page.ensureIndex({"doc_id":1});

/*
var user = db.user.findOne({"email":"soichih@gmail.com"});
//print(user.email);

db.doc.drop();
db.doc.insert({
    "name": "Soichi's doc",
    "owners": ["someoneelse", user._id]
});
db.doc.ensureIndex({"owners":1});

var doc = db.doc.findOne({"owners": user._id});
//print(doc.name);

db.page.drop();
*/
/*
db.page.insert([
    {
    'doc_id': doc._id,
    'name': 'November 2013',
    'start_date': new Date("10/1/2013").getTime(),
    'end_date': new Date("10/20/2013").getTime(),
    'total_income': 1500,
    'total_expense': 540,
    },
    {
    'doc_id': doc._id,
    'name': 'December 2013',
    'start_date': new Date("12/1/2013").getTime(),
    'end_date': new Date("12/31/2013").getTime(),
    'total_income': 1800,
    'total_expense': 1950,
    },
    {
    'doc_id': doc._id,
    'name': 'January 2014',
    'start_date': new Date("1/1/2014").getTime(),
    'end_date': new Date("1/31/2014").getTime(),
    'total_income': 1800,
    'total_expense': 540,
    }
]);
db.page.ensureIndex({"doc_id":1});
*/

/*
db.income.drop();
var page = db.page.findOne({"name": "November 2013"});
db.income.insert([
    {
    'page_id': page._id,
    'name': 'Health Insurance',
    'amount': 15.00
    },
    {
    'page_id': page._id,
    'name': 'Netflix',
    'amount': 9.00
    }
]);
*/
