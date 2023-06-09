const {ObjectId} = require("mongodb");
module.exports = function (app, offersRepository, usersRepository, chatsRepository, messagesRepository) {

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
                amount: i === 10?0:100,
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
                        // La primera oferta del usuario 14 estará vendida al 07
                        buyer: (i === 14 && j === 1)?"user07@email.com":null,
                        sold: i === 14 && j === 1
                    }
                    // Si oferta-user08-n1
                    if (i === 8 && j ===1) {
                        offer = {
                            _id: ObjectId("6456bba94dec7434ef3c3a8f"),
                            title: "Oferta-user"+emailNumber+"-n"+j,
                            description: "testsBorrar",
                            price: (j*10).toString(),
                            date: new Date().toLocaleDateString(),
                            seller: usersI[i-1].email,
                            // La primera oferta del usuario 14 estará vendida al 07
                            buyer: (i === 14 && j === 1)?"user07@email.com":null,
                            sold: i === 14 && j === 1
                        }
                    }
                    offers.push(offer);
                }
            }
            offersRepository.insertOffers(offers).then(() => {

                offersRepository.findOffer({title:"Oferta-user14-n1"}, {}).then(offer => {
                    let purchase = {
                        user: "user07@email.com",
                        offerId: offer._id
                    }
                    offersRepository.buyOffer(purchase, () => {
                        // Chat entre 7 y 14 con la oferta user14-n1
                        let chat = {
                            _id: ObjectId("1435bba94dec7434ef3c3a7a"),
                            user: "user07@email.com",
                            offer: offer._id
                        }
                        chatsRepository.insertChat(chat).then(() => {
                            let message = {
                                sender: chat.user,
                                text: "PRUEBA",
                                date: new Date().toLocaleDateString(),
                                read: false,
                                chatId: chat._id
                            }
                            messagesRepository.insertMessage(message, () => {
                                res.send("datos de los tests insertados");
                            })

                        }).catch(error => {
                            res.send("Error al insertar chat de los tests " + error);
                        })
                    })

                })


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
                let filterPurchases = {$or:[{user: "user14@email.com"},{user: "user09@email.com"},
                        {user: "user08@email.com"},{user: "user07@email.com"}]};
                offersRepository.deletePurchases(filterPurchases, options).then(() => {
                    let filterChats = {$or:[{user: "user01@email.com"}, {user: "user08@email.com"}, {user: "user07@email.com"}]};
                    chatsRepository.deleteChats(filterChats, options).then(() => {
                        let filterMessages = {text: "PRUEBA"};
                        messagesRepository.deleteMessages(filterMessages, options).then(() => {

                            res.send("datos de los tests quitados");

                        }).catch(error => {
                            res.send("Error al quitar los mensajes de los tests " + error);
                        })
                    }).catch(error => {
                        res.send("Error al quitar los chats de los tests " + error);
                    })
                }).catch(error => {
                    res.send("Error al quitar las compras de los tests " + error);
                })
            }).catch(error => {
                res.send("Error al quitar los ofertas de los tests " + error);
            });
        }).catch(error => {
            res.send("Error al quitar los usuarios de los tests " + error);
        });
    });
}