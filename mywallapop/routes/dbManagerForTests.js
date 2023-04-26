const {ObjectId} = require("mongodb");
module.exports = function (app, offersRepository, usersRepository) {

    app.get('/tests/insert', function (req, res) {
        res.send("datos de los tests insertados");
    });

    app.get('/tests/delete', function (req, res) {
        let filter = {email: "emailvalido@pruebas.com"};
        let options = {};
        usersRepository.deleteUser(filter, options).then(() => {
            res.send("datos de los tests quitados");
        }).catch(error => {
            res.send("Error al quitar los datos de los tests " + error);
        });
    });
}