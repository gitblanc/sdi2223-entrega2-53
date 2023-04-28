const {ObjectId} = require("mongodb");
module.exports = function (app, offersRepository, usersRepository) {

    app.get('/tests/insert', function (req, res) {
        let users = [];
        for (let i = 1; i < 16; i++) {
            let emailNumber = i.toString();
            if (i < 10) {
                emailNumber = "0" + emailNumber;
            }
            let password = "user"+emailNumber;
            let securePassword = app.get("crypto").createHmac('sha256', app.get('clave')).update(password).digest('hex');
            let userToSave = {
                email: "user"+emailNumber+"@email.com",
                name: "testsBorrar",
                surname: "testsBorrar",
                birthdate: "2001-01-01",
                amount: 100,
                role: 'Usuario Estándar',
                password: securePassword
            }
            users.push(userToSave);
        }
        usersRepository.insertUsers(users).then(() => {
            res.send("datos de los tests insertados");
        }).catch(error => {
            res.send("Error al insertar usuarios " + error);
        });
    });

    app.get('/tests/delete', function (req, res) {
        let filter = {name: "testsBorrar"};
        let options = {};
        usersRepository.deleteUsers(filter, options).then(() => {
            res.send("datos de los tests quitados");
        }).catch(error => {
            res.send("Error al quitar los datos de los tests " + error);
        });
    });
}