const {ObjectId} = require("mongodb");
module.exports = function (app, usersRepository) {
    app.get('/users', function (req, res) {
        res.send('lista de usuarios');
    })
    app.get('/users/login', function (req, res) {
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
                res.redirect("/users/login" +
                    "?message=Email o password incorrecto" +
                    "&messageType=alert-danger ");
            } else {

                if(user.role === 'Administrador'){
                    req.session.user = user.email;
                    req.session.userAmount = user.amount;
                    res.redirect("/users/list");
                }else{
                    req.session.user = user.email;
                    req.session.userAmount = user.amount;
                    res.redirect("/publications");
                }

            }
        }).catch(error => {
            req.session.user = null;
            res.redirect("/users/login" +
                "?message=Se ha producido un error al buscar el usuario" +
                "&messageType=alert-danger ");
        })
    })
    app.get('/users/logout', function (req, res) {
        req.session.user = null;
        req.session.amount = null;
        res.render("login.twig");
    })
    app.get("/users/list", function (req, res) {
        let filter = {email: {$ne:'admin@email.com'}};
        let options={};
        let page = parseInt(req.query.page); // Es String !!!
        if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === "0") { //Puede no venir el param
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
                email:req.session.user,
                amount:req.session.userAmount,
                users: result.users,
                pages: pages,
                currentPage: page
            }
            res.render("users/userslist.twig", response);
        }).catch(error => {
            res.send("Se ha producido un error al listar los usuarios " + error)
        });
    });
    app.get('/users/signup', function (req, res) {
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
                res.redirect("/users/signup" +
                    "?message=Se ha producido un error al registrar el usuario, email ya existe." +
                    "&messageType=alert-danger ");

            } else if (!checkDate(req.body.birthdate)) {
                res.redirect("/users/signup" +
                    "?message=Se ha producido un error al registrar el usuario, fecha de nacimiento " +
                    "inválida." +
                    "&messageType=alert-danger ");

            } else if (req.body.password != req.body.confirmpassword) {
                res.redirect("/users/signup" +
                    "?message=Se ha producido un error al registrar el usuario, contraseñas distintas." +
                    "&messageType=alert-danger ");

            } else {
                usersRepository.insertUser(userToSave).then(userId => {
                     req.session.user = userToSave.email;
                     req.session.userAmount = userToSave.amount;
                     res.redirect("/offers/list");

                }).catch(error => {
                    res.redirect("/users/signup" +
                        "?message=Se ha producido un error al registrar el usuario." +
                        "&messageType=alert-danger ");
                });
            }
        }).catch(error => {
            req.session.user = null;
            res.redirect("/users/signup" +
                "?message=Se ha producido un error al buscar el usuario en la pantalla de registro" +
                "&messageType=alert-danger ");
        })
    });
    app.post('/users/delete', function (req, res) {
        let usersToDelete = req.body.check;
        if(typeof usersToDelete === 'undefined'){
            res.redirect("/users/list");
        }else if(!Array.isArray(usersToDelete)){
            usersToDelete = usersToDelete.substring(0, usersToDelete.length - 1);
            let filter = {email : usersToDelete};
            if(usersToDelete != 'admin@email.com'){
                usersRepository.deleteUser(filter, {}).then(result => {
                    if (result === null || result.deletedCount === 0) {
                        res.send("No se ha podido eliminar el usuario");
                        return null;
                    }else{
                        res.redirect("/users/list");
                    }
                }).catch(error => {
                    res.send("Se ha producido un error al intentar eliminar el usuario: " + error)
                });
            }else{
                res.send("No es posible borrar el usuario administrador");
            }

        }else{
            for (let i = 0; i < usersToDelete.length; i++) {
                usersToDelete[i] = usersToDelete[i].substring(0, usersToDelete[i].length - 1);
            }
            let filter = {email : {$in:usersToDelete}};

            if(!usersToDelete.includes('admin@wmail.com')){
                usersRepository.deleteUsers(filter, {}).then(result => {
                    if (result === null || result.deletedCount === 0) {
                        res.send("No se han podido eliminar los usuarios");
                        return null;
                    }else{
                        res.redirect("/users/list");
                    }
                }).catch(error => {
                    res.send("Se ha producido un error al intentar eliminar los usuarios: " + error)
                });
            }else{
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