const express = require('express');
const userSessionRouter = express.Router();
const appLogger = require('../logger');
userSessionRouter.use(function (req, res, next) {
    console.log("routerUsuarioSession");
    if (req.session.user) {
        // dejamos correr la petición
        next();
    } else {
        console.log("va a: " + req.originalUrl);
        appLogger.createNewLog("Intento de acceso a " + req.originalUrl + " sin estar logeado", "PET-ERR");
        res.redirect("/users/login");
    }
});
module.exports = userSessionRouter;