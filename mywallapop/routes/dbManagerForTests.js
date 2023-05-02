const {ObjectId} = require("mongodb");
module.exports = function (app, offersRepository, usersRepository) {

    /**
     * Inserta usuarios con nombre 'testsBorrar' y ofertas con título 'testsBorrar' en la base de datos
     */
    app.get('/tests/insert', function (req, res) {
        let users = [];
        // Estándares
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
        // Admin
        let password = "admin";
        let securePassword = app.get("crypto").createHmac('sha256', app.get('clave')).update(password).digest('hex');
        let admin = {
            email: "admin@email.com",
            name: "testsBorrar",
            surname: "testsBorrar",
            birthdate: "2001-01-01",
            amount: 100,
            role: 'Administrador',
            password: securePassword
        }
        users.push(admin);
        usersRepository.insertUsers(users).then(() => {
            let usersI = users;
            // insertar ofertas
            let offers = [];
            for (let i = 1; i < 16; i++) {
                let emailNumber = i.toString();
                if (i < 10) {
                    emailNumber = "0" + emailNumber;
                }
                for (let j = 1; j < 11; j++) {
                    let offer = {
                        title: "Oferta-user"+emailNumber+"-n"+j,
                        description: "testsBorrar",
                        price: (j*10).toString(),
                        date: new Date().toLocaleDateString(),
                        seller: usersI[i-1].email,
                        buyer: null,
                        // La primera oferta del usuario 14 estará marcada como vendida
                        sold: i === 14 && j === 1
                    }
                    offers.push(offer);
                }
            }
            offersRepository.insertOffers(offers).then(() => {

                res.send("datos de los tests insertados");

            }).catch(error => {
                res.send("Error al insertar ofertas de los tests " + error);
            })
        }).catch(error => {
            res.send("Error al insertar usuarios de los tests " + error);
        });
    });

    /**
     * Borra usuarios con nombre 'testsBorrar' y ofertas con descripción 'testsBorrar' de la base de datos
     */
    app.get('/tests/delete', function (req, res) {
        let filter = {name: "testsBorrar"};
        let options = {};
        usersRepository.deleteUsers(filter, options).then(() => {
            let filterOffers = {description: "testsBorrar"};
            offersRepository.deleteOffers(filterOffers, options).then(() => {

                res.send("datos de los tests quitados");

            }).catch(error => {
                res.send("Error al quitar los ofertas de los tests " + error);
            });
        }).catch(error => {
            res.send("Error al quitar los usuarios de los tests " + error);
        });
    });
}