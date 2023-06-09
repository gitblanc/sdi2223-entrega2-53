const {ObjectId} = require("mongodb");
module.exports = function (app, offersRepository, usersRepository, chatsRepository, messagesRepository) {

    /**
     * Comprueba la operación de logeo de un usuario con los datos introducidos
     */
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

    /**
     * Devuelve la lista de ofertas de los usuarios que no sean el que esté en sesión
     */
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

    app.get("/api/v1.0/offers/:offerId", function (req, res) {
        let filter = {_id: new ObjectId(req.params.offerId)};
        let options = {};
        offersRepository.getOffers(filter, options).then(offer => {
            res.status(200);
            res.send({offer: offer})
        }).catch(error => {
            res.status(500);
            res.json({error: "Se ha producido un error al recuperar la oferta."})
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
     * Petición POST que añadirá un mensjae  ala base de datos. Para añadirlo la oferta tiene que existir
     */
    app.post("/api/v1.0/chat/:offerId/:chatId", function (req, res) {
            try {
                //el mensaje tiene que tener el id del chat al que pertenece
                //busco el chat con el id de la oferta y el id del usuario y si lo encuntra lo inserta en ese chat y sini pues creara otro
                let message = {
                    sender: req.session.user,
                    text: req.body.messageText,
                    date: new Date().toLocaleDateString(),
                    read: false,
                    chatId: new ObjectId(req.params.chatId)
                }

                let offerId = ObjectId(req.params.offerId);

                let options = {};

                let isValid = true;

                if (message.sender === null || message.sender === "") {
                    isValid = false;
                }

                if (message.text === null || message.text === undefined || message.text.trim().length === 0) {
                    isValid = false;
                }
                const offerFilter = {_id: new ObjectId(offerId)};
                offersRepository.findOffer(offerFilter, {}).then(offer => {
                    if (offer == null) {
                        isValid = false;
                    }
                    if (isValid) {
                        messagesRepository.getMessages({chat: message.chatId},{}).then(messages => {
                            if (messages.length === 0 && message.sender === offer.seller) {
                                res.status(404);
                                res.json({
                                    message: "No puedes enviar el primer mensaje a una conversación si eres el propietario de la oferta."
                                })
                            } else {
                                messagesRepository.insertMessage(message, function (messageId) {
                                    if (messageId == null) {
                                        res.send("Se ha producido un error al añadir el mensaje")
                                    } else {
                                        res.status(201);
                                        res.json({
                                            message: "Mansaje añadido correctamente.",
                                            _id: messageId
                                        })
                                    }
                                })
                            }

                        })

                    } else {
                        res.status(500);
                        res.send("Oferta no encontrada.")
                    }
                })
            } catch (e) {
                res.status(500);
                res.json({error: "Se ha producido un error al intentar añadir el mensaje: " + e})
            }
        }
    )

    /**
     * Dado el id de una oferta y el otro usuario (que debe estar en la URL si el solicitador es el vendedor)
     * muestra el listado de los mensajes de una conversación. Si el chat no existe, la crea
     */
    app.get("/api/v1.0/offers/chats/byoffer/:offerId", function (req, res) {
        // Obtengo parámetros de la URL
        let offerId = ObjectId(req.params.offerId);
        let otherUser = req.query.otherUser;
        let activeUser = req.session.user;
        // Busco oferta para ver el propietario
        offersRepository.findOffer({_id: offerId}, {}).then(offer => {
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
                // Si no encontró un chat
                if (chat === null || typeof chat === "undefined") {
                    // Crearlo
                    chat = {
                        offer: offerId,
                        user: userClient
                    }
                    chatsRepository.insertChat(chat).then(insertedId => {
                        getMessages(res, insertedId);
                    })
                } else {
                    getMessages(res, chat._id);
                }
            }).catch(error => {
                res.status(500);
                res.json({error: "Se ha producido un error el chat." + error})
            })
        }).catch(error => {
            res.status(500);
            res.json({error: "Se ha producido un error al recuperar las ofertas." + error})
        })
    });

    function getMessages(res, chatId) {
        let filterMessages = {chatId: chatId}
        messagesRepository.getMessages(filterMessages, {}).then(messages => {
            res.status(200);
            res.json({chat: chatId, messages: messages});

        }).catch(error => {
            res.status(500);
            res.json({error: "Se ha producido un error al recuperar los mensajes." + error})
        })
    }

    /**
     * Dado el id de un chat da sus mensajes. No la crea si no existe.
     */
    app.get("/api/v1.0/offers/chats/:chatId", function (req, res) {
        let chatId = ObjectId(req.params.chatId);

        messagesRepository.getMessages({chatId: chatId}, {}).then(messages => {

            res.status(200);
            res.json({messages: messages});

        }).catch(error => {
            res.status(500);
            res.json({error: "Se ha producido un error al recuperar los mensajes." + error})
        })
    });

}