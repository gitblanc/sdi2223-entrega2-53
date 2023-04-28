const {ObjectId} = require("mongodb");
module.exports = function (app, offersRepository, usersRepository, chatsRepository, messagesRepository) {
    app.post('/api/v1.0/users/login', function (req, res) {
            try {
                if (typeof req.body.password == "undefined" || req.body.password == null) {
                    res.status(401);
                    res.json({
                        message: "Se ha producido un error al verificar credenciales",
                        authenticated: false
                    })
                } else {

                    let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                        .update(req.body.password).digest('hex');
                    let filter = {
                        email: req.body.email, password: securePassword
                    }
                    let options = {};
                    usersRepository.findUser(filter, options).then(user => {
                        if (user == null) {
                            res.status(401);
                            res.json({
                                message: "usuario no autorizado",
                                authenticated: false
                            })
                        } else {
                            let token = app.get("jwt").sign(
                                {user: user.email, time: Date.now() / 1000},
                                "secreto"
                            );
                            req.session.user = user.email;
                            res.status(200);
                            res.json({
                                message: "usuario autorizado",
                                authenticated: true,
                                token: token
                            })
                        }
                    }).catch
                    (error => {
                        res.status(401);
                        res.json({
                            message: "Se ha producido un error al verificar credenciales",
                            authenticated: false
                        })
                    })
                }
            } catch (e) {
                res.status(500);
                res.json({
                    message: "Se ha producido un error al verificar las credenciales",
                    authenticated: false
                })
            }

        }
    )
    app.get("/api/v1.0/offers", function (req, res) {
        let filter = {seller: {$ne: req.session.user}};
        let options = {};
        offersRepository.getOffers(filter, options).then(offers => {
            res.status(200);
            res.send({offers: offers})
        }).catch(error => {
            res.status(500);
            res.json({error: "Se ha producido un error al recuperar las ofertas."})
        });
    });

    /**
     * Devuelve el listado de conversaciones del usuario identificado
     */
    app.get("/api/v1.0/offers/chats/list", function (req, res) {
        let activeUser = res.user;

        // conversaciones en las que es el interesado
        chatsRepository.getChats({user: activeUser}, {}).then(chatsInterested => {
            let chats = chatsInterested;
            // conversaciones en las que es el propietario
            // obtener ids de todas sus ofertas
            offersRepository.getOffers({seller: activeUser}, {}).then(offers => {
                let offersIds = [];
                for (let i = 0; i < offers.length; i++) {
                    offersIds.push(offers[i]._id);
                }
                // obtener conversaciones cuya offer pertenece a la lista
                chatsRepository.getChats({"offer": {$in: offersIds}}, {}).then(chatsSeller => {
                    for (let i = 0; i < chatsSeller.length; i++) {
                        chats.push(chatsSeller[i]);
                    }

                    res.status(200);
                    res.json({chats: chats});

                });
            }).catch(error => {
                res.status(500);
                res.json({error: "Se ha producido un error al recuperar las ofertas." + error})
            })
        }).catch(error => {
            res.status(500);
            res.json({error: "Se ha producido un error al recuperar los chats." + error})
        })
    });

    /**
     * Dado el id de una oferta y el otro usuario (que debe estar en la URL si el solicitador es el vendedor)
     * muestra el listado de los mensajes de una conversación.
     */
    app.get("/api/v1.0/offers/chats/byoffer/:offerId", function (req, res) {
        // Obtengo parámetros de la URL
        let offerId = ObjectId(req.params.offerId);
        let otherUser = req.query.otherUser;
        let activeUser = res.user;

        // Busco oferta para ver el propietario
        offersRepository.findOffer({_id:offerId}, {}).then(offer => {
            let userClient;
            // si el usuario que solicita el chat es el vendedor
            if (activeUser === offer.seller) {
                if (otherUser === null || typeof (otherUser) === "undefined") {
                    res.status(404);
                    res.json({error: "Como eres el propietario de la oferta debes indicar el otro usuario mediante ?otherUser=email"})
                    return;
                }
                userClient = otherUser;
            } else {
                userClient = activeUser;
            }
            let filter = {offer: offerId, user: userClient};
            chatsRepository.findChat(filter, {}).then(chat => {
                let filterMessages = {chat: chat._id}
                messagesRepository.getMessages(filterMessages, {}).then(messages => {

                    res.status(200);
                    res.json({messages: messages});

                }).catch(error => {
                    res.status(500);
                    res.json({error: "Se ha producido un error al recuperar los mensajes." + error})
                })
            }).catch(error => {
                res.status(500);
                res.json({error: "Se ha producido un error el chat." + error})
            })
        }).catch(error => {
            res.status(500);
            res.json({error: "Se ha producido un error al recuperar las ofertas." + error})
        })
    });

    /**
     * Dado el id de una conversación da sus mensajes
     */
    app.get("/api/v1.0/offers/chats/:chatId", function (req, res) {
        let chatId = ObjectId(req.params.chatId);

        messagesRepository.getMessages({chat: chatId}, {}).then(messages => {

            res.status(200);
            res.json({messages: messages});

        }).catch(error => {
            res.status(500);
            res.json({error: "Se ha producido un error al recuperar los mensajes." + error})
        })
    });
}