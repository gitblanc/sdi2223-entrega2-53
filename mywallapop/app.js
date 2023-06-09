var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
let crypto = require("crypto");

var app = express();

let rest = require('request');
app.set('rest', rest);

app.use(function (req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Credentials", "true");
    res.header("Access-Control-Allow-Methods", "POST, GET, DELETE, UPDATE, PUT");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, token");
    // Debemos especificar todas las headers que se aceptan. Content-Type , token
    next();
});


let expressSession = require('express-session');
app.use(expressSession({
    secret: 'abcdefg',
    resave: true,
    saveUninitialized: true
}));

let fileUpload = require('express-fileupload');
app.use(fileUpload({
    limits: {fileSize: 50 * 1024 * 1024},
    createParentPath: true
}));
app.set("uploadPath", __dirname);

app.set("clave", "abcdefg");
app.set("crypto", crypto);

let bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

let jwt = require('jsonwebtoken');
app.set('jwt', jwt);

const {MongoClient} = require("mongodb");
//const url = 'mongodb+srv://admin:sdi@eii-sdi-cluster.py3eqdo.mongodb.net/?retryWrites=true&w=majority'
const url = 'mongodb://localhost:27017';
app.set('connectionStrings', url);


//_____________REPOSITORIES___________________
const usersRepository = require("./repositories/usersRepository.js");
const offersRepository = require("./repositories/offersRepository.js");
const messagesRepository = require("./repositories/messagesRepository.js");
const chatsRepository = require("./repositories/chatsRepository.js");
usersRepository.init(app, MongoClient);
offersRepository.init(app, MongoClient);
messagesRepository.init(app, MongoClient);
chatsRepository.init(app, MongoClient);

const userSessionRouter = require('./routes/userSessionRouter');
//logs
const logsRepository = require('./repositories/logsRepository');
logsRepository.init(app, MongoClient)
//_________________________________________

// _________ USER_SESSION_CHECK _____________
app.use("/users/list", userSessionRouter);
app.use("/users/logs", userSessionRouter);
app.use("/offers/add",userSessionRouter);
app.use("/offers/buy",userSessionRouter);
app.use("/purchases",userSessionRouter);
app.use("/publications",userSessionRouter);
app.use("/shop/",userSessionRouter);
const userTokenRouter = require('./routes/userTokenRouter');
app.use("/api/v1.0/offers", userTokenRouter);

// _________________________________________


// _________ ROUTERS _____________
var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');
var offersRouter = require('./routes/offers');
// _______________________________

//LOGGER
const appLogger = require('./logger');

//ROUTES
require("./routes/users.js")(app, usersRepository,offersRepository, chatsRepository,messagesRepository, appLogger);
require("./routes/offers.js")(app, offersRepository, usersRepository, appLogger);
require("./routes/dbManagerForTests.js")(app, offersRepository, usersRepository, chatsRepository, messagesRepository);

//ROUTES API
require("./routes/api/offersAPIv1.0.js")(app, offersRepository, usersRepository, chatsRepository, messagesRepository);


// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'twig');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({extended: false}));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
    // set locals, only providing error in development
    res.locals.message = err.message;
    res.locals.error = req.app.get('env') === 'development' ? err : {};

    // render the error page
    res.status(err.status || 500);
    res.render('error');
});

module.exports = app;
