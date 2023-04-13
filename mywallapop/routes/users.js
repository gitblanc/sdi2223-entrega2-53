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
                    res.locals.user = req.session.user;
                    res.redirect("/users/list");
                }else{
                    req.session.user = user.email;
                    res.locals.user = req.session.user;
                    res.redirect("/offers/list");
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
        res.send("El usuario se ha desconectado correctamente");
    })
    app.get("/users/list", function (req, res) {
        res.render("users/userslist.twig");
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