const {ObjectId} = require("mongodb");
const logsRepository = require("../repositories/logsRepository");
const appLogger = require("../logger");
module.exports = function (app, usersRepository, offersRepository) {
    app.get('/users', function (req, res) {
        appLogger.createNewLog("Acceso a la lista de usuarios", "PET");
        res.send('lista de usuarios');
    })
    app.get('/users/login', function (req, res) {
        appLogger.createNewLog("Acceso a la página de login", "PET");
        res.render("login.twig");
    })
    app.post("/users/login", function (req, res) {
        let securePassword = app.get("crypto").createHmac("sha256", app.get("clave"))
            .update(req.body.password).digest("hex");
        let filter = {
            email: req.body.email,
            password: securePassword
        }
        let options = {};
        usersRepository.findUser(filter, options).then(user => {
            if (user == null) {
                req.session.user = null;
                appLogger.createNewLog("Intento de login a " + req.body.email + " fallido", "LOGIN-ERR");
                res.redirect("/users/login" +
                    "?message=Email o password incorrecto" +
                    "&messageType=alert-danger ");
            } else {
                if (user.role === 'Administrador') {
                    req.session.user = user.email;
                    req.session.userAmount = user.amount;
                    appLogger.createNewLog("El administrador " + user.email + " se logueó correctamente", "LOGIN-EX");
                    res.redirect("/users/list");
                } else {
                    req.session.user = user.email;
                    req.session.userAmount = user.amount;
                    appLogger.createNewLog("El usuario " + user.email + " se logueó correctamente", "LOGIN-EX");
                    res.redirect("/publications");
                }
            }
        }).catch(error => {
            req.session.user = null;
            appLogger.createNewLog("Error al buscar el usuario " + req.body.email + " en la base de datos", "PET-ERR");
            res.redirect("/users/login" +
                "?message=Se ha producido un error al buscar el usuario" +
                "&messageType=alert-danger ");
        })
    })
    app.get('/users/logs', function (req, res) {
        if(req.session.user === 'admin@email.com') {
            let filter = {};
            let options = {
                sort: {date: -1} // ordena por fecha-hora en orden ascendente
            };
            if (req.session.user === '' || req.session.user === null || req.session.user === undefined) {
                appLogger.createNewLog("Intento de acceso a los logs sin estar logueado", "PET-ERR");
                res.redirect("/users/login");
            } else {
                renderLogs(req, res, filter, options);
            }
        }else{
            res.send("Solo el administrador puede acceder a lista de logs");
        }
    })

    function renderLogs(req, res, filter, options) {
        logsRepository.getLogs(filter, options).then(logs => {
            let response = {
                email: req.session.user,
                amount: req.session.userAmount,
                logs: logs
            }
            appLogger.createNewLog("El administrador " + req.session.user + " accedió a los logs", "PET");
            res.render("users/logs.twig", response);
        }).catch(error => {
            appLogger.createNewLog("Error al listar los logs", "PET-ERR");
            res.send("Se ha producido un error al listar los logs");
        })
    }

    app.post('/users/logs', function (req, res) {
        let filter = {};
        let options = {
            sort: {date: -1} // ordena por fecha-hora en orden ascendente
        };
        if (req.session.user === '' || req.session.user === null || req.session.user === undefined) {
            appLogger.createNewLog("Intento de acceso a los logs sin estar logueado", "PET-ERR");
            res.redirect("/users/login");
        } else {
            if (req.body.typeFilter === "" || req.body.typeFilter === null || req.body.typeFilter === undefined || req.body.typeFilter === "todo")
                filter = {};
            else
                filter = {type: req.body.typeFilter.toUpperCase()};
            renderLogs(req, res, filter, options);
        }
    })
    app.post('/users/logs/delete', function (req, res) {
        let filter = {};
        if (req.session.user === '' || req.session.user === null || req.session.user === undefined) {
            appLogger.createNewLog("Intento de eliminar los logs sin estar logueado como administrador", "PET-ERR");
            res.redirect("/users/login");
        } else {
            logsRepository.deleteLogs(filter).then(result => {
                appLogger.createNewLog("El administrador " + req.session.user + " eliminó los logs", "PET");
                res.redirect("/users/logs");
            }).catch(error => {
                appLogger.createNewLog("Error al eliminar los logs", "PET-ERR");
                res.send("Se ha producido un error al eliminar los logs");
            })
        }
    })
    app.get('/users/logout', function (req, res) {
        let aux = req.session.user;
        req.session.user = null;
        req.session.amount = null;
        appLogger.createNewLog("El usuario " + aux + " salió de sesión", "LOGOUT");
        res.render("login.twig");
    })
    app.get("/users/list", function (req, res) {
        if(req.session.user === 'admin@email.com') {
            let filter = {email: {$ne: 'admin@email.com'}};
            let options = {};
            let page = parseInt(req.query.page); // Es String !!!
            if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === 0) { //Puede no venir el param
                page = 1;
            }
            usersRepository.getUsers(filter, options, page).then(result => {
                let lastPage = result.total / 4;
                if (result.total % 4 > 0) { // Sobran decimales
                    lastPage = lastPage + 1;
                }
                let pages = []; // paginas mostrar
                for (let i = page - 2; i <= page + 2; i++) {
                    if (i > 0 && i <= lastPage) {
                        pages.push(i);
                    }
                }
                let response = {
                    email: req.session.user,
                    amount: req.session.userAmount,
                    users: result.users,
                    pages: pages,
                    currentPage: page
                }
                appLogger.createNewLog("El administrador " + req.session.user + " accedió a la lista de usuarios", "PET");
                res.render("users/userslist.twig", response);
            }).catch(error => {
                appLogger.createNewLog("Error al listar los usuarios", "PET-ERR");
                res.send("Se ha producido un error al listar los usuarios " + error)
            });
        }else{
            res.send("Solo el administrador puede acceder a lista de usuarios");
        }
    });
    app.get('/users/signup', function (req, res) {
        appLogger.createNewLog("Acceso a la página de registro", "PET");
        res.render("signup.twig");
    })
    app.post('/users/signup', function (req, res) {
        let securePassword = app.get("crypto").createHmac('sha256', app.get('clave')).update(req.body.password).digest('hex');
        let userToSave = {
            email: req.body.email,
            name: req.body.name,
            surname: req.body.surname,
            birthdate: req.body.birthdate,
            amount: 100,
            role: 'Usuario Estándar',
            password: securePassword
        }

        let emailFilter = {
            email: req.body.email,
        }
        usersRepository.findUser(emailFilter, {}).then(user => {

            if (user != null) {
                appLogger.createNewLog("Error al registrar el usuario " + req.body.email + ", email ya existe", "ALTA-ERR");
                res.redirect("/users/signup" +
                    "?message=Se ha producido un error al registrar el usuario, email ya existe." +
                    "&messageType=alert-danger ");

            } else if (!checkDate(req.body.birthdate)) {
                appLogger.createNewLog("Error al registrar el usuario " + req.body.email + ", fecha de nacimiento inválida", "ALTA-ERR");
                res.redirect("/users/signup" +
                    "?message=Se ha producido un error al registrar el usuario, fecha de nacimiento " +
                    "inválida." +
                    "&messageType=alert-danger ");

            } else if (req.body.password != req.body.confirmpassword) {
                appLogger.createNewLog("Error al registrar el usuario " + req.body.email + ", contraseñas distintas", "ALTA-ERR");
                res.redirect("/users/signup" +
                    "?message=Se ha producido un error al registrar el usuario, contraseñas distintas." +
                    "&messageType=alert-danger ");

            } else {
                usersRepository.insertUser(userToSave).then(userId => {
                    req.session.user = userToSave.email;
                    req.session.userAmount = userToSave.amount;
                    appLogger.createNewLog("El usuario " + userToSave.email + " se registró correctamente", "ALTA");
                    res.redirect("/publications");

                }).catch(error => {
                    appLogger.createNewLog("Error al registrar el usuario " + req.body.email + " en la base de datos", "ALTA-ERR");
                    res.redirect("/users/signup" +
                        "?message=Se ha producido un error al registrar el usuario." +
                        "&messageType=alert-danger ");
                });
            }
        }).catch(error => {
            req.session.user = null;
            appLogger.createNewLog("Error al buscar el usuario " + req.body.email + " en la base de datos", "PET-ERR");
            res.redirect("/users/signup" +
                "?message=Se ha producido un error al buscar el usuario en la pantalla de registro" +
                "&messageType=alert-danger ");
        })
    });
    app.post('/users/delete', function (req, res) {
        let usersToDelete = req.body.check;
        if (typeof usersToDelete === 'undefined') {
            appLogger.createNewLog("Error al borrar usuarios, no se ha seleccionado ningún usuario", "PET-ERR");
            res.redirect("/users/list");
        } else if (!Array.isArray(usersToDelete)) {
            usersToDelete = usersToDelete.substring(0, usersToDelete.length - 1);
            let filter = {email: usersToDelete};
            if (usersToDelete != 'admin@email.com') {
                usersRepository.deleteUser(filter, {}).then(result => {
                    if (result === null || result.deletedCount === 0) {
                        appLogger.createNewLog("Error al borrar el usuario " + usersToDelete, "PET-ERR");
                        res.send("No se ha podido eliminar el usuario");
                        return null;
                    } else {
                        let filterOffers = {seller: usersToDelete};
                        offersRepository.deleteOffers(filterOffers,{}).then(result =>{
                            let filterPurchases = {user: usersToDelete};
                            offersRepository.deletePurchases(filterPurchases,{}).then(result =>{
                                appLogger.createNewLog("El usuario " + usersToDelete + " ha sido borrado correctamente", "PET");
                                res.redirect("/users/list");
                            }).catch(error => {
                                appLogger.createNewLog("Error al borrar las compras del usuario " + usersToDelete, "PET-ERR");
                                res.send("Se ha producido un error al intentar eliminar las compras del usuario: " + error)
                            });
                        }).catch(error => {
                            appLogger.createNewLog("Error al borrar las ofertas del usuario " + usersToDelete, "PET-ERR");
                            res.send("Se ha producido un error al intentar eliminar las ofertas del usuario: " + error)
                        });
                    }
                }).catch(error => {
                    appLogger.createNewLog("Error al borrar el usuario " + usersToDelete, "PET-ERR");
                    res.send("Se ha producido un error al intentar eliminar el usuario: " + error)
                });
            } else {
                appLogger.createNewLog("Error al borrar el usuario " + usersToDelete, "PET-ERR");
                res.send("No es posible borrar el usuario administrador");
            }

        } else {
            for (let i = 0; i < usersToDelete.length; i++) {
                usersToDelete[i] = usersToDelete[i].substring(0, usersToDelete[i].length - 1);
            }
            let filter = {email: {$in: usersToDelete}};

            if (!usersToDelete.includes('admin@wmail.com')) {
                usersRepository.deleteUsers(filter, {}).then(result => {
                    if (result === null || result.deletedCount === 0) {
                        appLogger.createNewLog("Error al borrar los usuarios " + usersToDelete, "PET-ERR");
                        res.send("No se han podido eliminar los usuarios");
                        return null;
                    } else {
                        let filterOffers = {seller: {$in: usersToDelete}};
                        offersRepository.deleteOffers(filterOffers,{}).then(result =>{
                            let filterPurchases = {user: {$in: usersToDelete}};
                            offersRepository.deletePurchases(filterPurchases,{}).then(result =>{
                                appLogger.createNewLog("Los usuarios" + usersToDelete + " han sido borrado correctamente", "PET");
                                res.redirect("/users/list");
                            }).catch(error => {
                                appLogger.createNewLog("Error al borrar las compras de los usuarios " + usersToDelete, "PET-ERR");
                                res.send("Se ha producido un error al intentar eliminar las compras de los usuarios: " + error)
                            });
                        }).catch(error => {
                            appLogger.createNewLog("Error al borrar las ofertas del usuario " + usersToDelete, "PET-ERR");
                            res.send("Se ha producido un error al intentar eliminar las ofertas del usuario: " + error)
                        });
                    }
                }).catch(error => {
                    appLogger.createNewLog("Error al borrar los usuarios " + usersToDelete, "PET-ERR");
                    res.send("Se ha producido un error al intentar eliminar los usuarios: " + error)
                });
            } else {
                appLogger.createNewLog("No se puede borrar al usuario administrador", "PET-ERR");
                res.send("No es posible borrar el usuario administrador");
            }
        }
    })


    /**
     * Comprueba si la fecha es anterior a la actual
     * @param paramDate
     * @returns {boolean}
     */
    function checkDate(paramDate) {
        let currentDate = new Date();
        let day = currentDate.getDate();
        let month = currentDate.getMonth() + 1; // month starts from 0, so add 1
        let year = currentDate.getFullYear();

        // Pad the day and month with leading zeros if they are less than 10
        if (day < 10) {
            day = "0" + day;
        }
        if (month < 10) {
            month = "0" + month;
        }

        let formattedDate = year + "-" + month + "-" + day;
        if (paramDate < formattedDate) {
            return true;
        } else {
            return false;
        }
    }
}