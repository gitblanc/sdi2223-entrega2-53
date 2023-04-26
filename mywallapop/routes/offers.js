const {ObjectId} = require("mongodb");
const logsRepository = require("../repositories/logsRepository");
const appLogger = require("../logger");
module.exports = function (app, offersRepository, usersRepository) {

    app.get('/publications/highlight/:id', function (req, res) {
        checkAmount20OrAbove(req, res, user => {
            checkOwnOfferAndNotHighlight(req, req, user, user => {
                remove20FromUserAmount(req, res, user, () => {
                    // Destacar oferta
                    let newOffer = {highlight: true};
                    let filter = {_id: ObjectId(req.params.id)};
                    let options = {upsert: false};
                    offersRepository.updateOffer(newOffer, filter, options).then(() => {
                        appLogger.createNewLog("Oferta con id " + req.params.id + " destacada", "PET");
                        res.redirect("/publications");
                    }).catch(error => {
                        appLogger.createNewLog("Error al destacar la oferta con id " + req.params.id, "PET-ERR");
                        res.send("Error al actualizar la oferta " + error);
                    });
                });
            });
        });
    });

    function remove20FromUserAmount(req, res, user, callBackFunc) {
        let newUser = {amount: user.amount - 20};
        let filter = {_id: user._id};
        // que no se cree un documento nuevo, si no existe
        let options = {upsert: false};
        usersRepository.updateUser(newUser, filter, options).then(() => {
            req.session.userAmount = newUser.amount;
            callBackFunc();
        }).catch(error => {
            appLogger.createNewLog("Error al actualizar el dinero del usuario " + req.session.user, "PET-ERR");
            res.send("Error al actualizar el usuario " + error);
        })
    }

    function checkOwnOfferAndNotHighlight(req, res, user, callBackFunc) {
        let filter = {_id: ObjectId(req.params.id)};
        let options = {};
        offersRepository.findOffer(filter, options).then(offer => {
            if (offer.seller !== req.session.user) {
                appLogger.createNewLog("El usuario " + req.session.user + " intentó destacar una oferta que no es suya", "PET-ERR");
                res.send("No eres el propietario");
            } else if (offer.highlight === true) {
                appLogger.createNewLog("El usuario " + req.session.user + " intentó destacar una oferta que ya estaba destacada", "PET-ERR");
                res.send("La oferta ya está destacada");
            } else {
                callBackFunc(user);
            }
        }).catch(error => {
            appLogger.createNewLog("Error al buscar la oferta con id " + req.params.id, "PET-ERR");
            res.send("Error al buscar la oferta " + error);
        })
    }

    function checkAmount20OrAbove(req, res, callBackFunc) {
        let filter = {email: req.session.user};
        let options = {};
        usersRepository.findUser(filter, options).then(user => {
            if (user.amount < 20) {
                appLogger.createNewLog("El usuario " + req.session.user + " intentó destacar una oferta sin tener 20 euros", "PET-ERR");
                res.send("No tienes al menos 20 euros");
            } else {
                callBackFunc(user);
            }
        }).catch(error => {
            appLogger.createNewLog("Error al buscar al usuario " + req.session.user, "PET-ERR");
            res.send("Error al buscar al usuario " + error);
        });
    }

    /**
     * Responde la petición GET para ver las ofertas compradas por el usuario logeado
     */
    app.get('/purchases', function (req, res) {
        let filter = {user: req.session.user};
        let options = {projection: {_id: 0, offerId: 1}};
        offersRepository.getPurchases(filter, options).then(purchases => {
            let purchasedOffersIds = [];
            for (let i = 0; i < purchases.length; i++) {
                purchasedOffersIds.push(purchases[i].offerId)
            }
            let filter = {"_id": {$in: purchasedOffersIds}};
            let options = {sort: {title: 1}};

            let page = parseInt(req.query.page);
            if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === 0)
                page = 1;

            offersRepository.getOffersPg(filter, options, page, 4).then(purchasedOffers => {
                let lastPage = purchasedOffers.total / 4;
                if (purchasedOffers.total % 4 > 0) {
                    lastPage = lastPage + 1;
                }
                let pages = [];
                for (let i = page - 2; i <= page + 2; i++) {
                    if (i > 0 && i <= lastPage) {
                        pages.push(i);
                    }
                }
                let response = {
                    email: req.session.user,
                    amount: req.session.userAmount,
                    offers: purchasedOffers.offers,
                    pages: pages,
                    currentPage: page
                }
                appLogger.createNewLog("El usuario " + req.session.user + " ha visto sus compras", "PET");
                res.render("purchase.twig", response);
            }).catch(error => {
                appLogger.createNewLog("Se ha producido un error al listar las ofertas compradas del usuario " + req.session.user, "PET-ERR");
                res.send("Se ha producido un error al listar las ofertas compradas del usuario: " + error)
            });
        }).catch(error => {
            appLogger.createNewLog("Se ha producido un error al listar las compras del usuario " + req.session.user, "PET-ERR");
            res.send("Se ha producido un error al listar las compras del usuario " + error)
        });
    });

    /**
     * Responde a la petición GET para ver las ofertas
     * publicadas por el usuario logeado
     */
    app.get("/publications", function (req, res) {
        let numItemsPorPagina = 4;

        let filter = {seller: req.session.user};
        let page = parseInt(req.query.page);
        if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === 0)
            page = 1;

        // obtiene las ofertas
        offersRepository.getOffersPg(filter, {}, page, 4).then(result => {
            let lastPage = result.total / 4;
            if (result.total % 4 > 0) {
                lastPage = lastPage + 1;
            }
            let pages = [];
            for (let i = page - 2; i <= page + 2; i++) {
                if (i > 0 && i <= lastPage) {
                    pages.push(i);
                }
            }

            let response = {
                email: req.session.user,
                amount: req.session.userAmount,
                offers: result.offers,
                pages: pages,
                currentPage: page,
            }
            appLogger.createNewLog("El usuario " + req.session.user + " ha visto sus publicaciones", "PET");
            res.render("offers/myOffersList.twig", response);
        }).catch(error => {
            appLogger.createNewLog("Se ha producido un error al listar las publicaciones del usuario " + req.session.user, "PET-ERR");
            res.send("Se ha producido un error al listar las publicaciones " + error)
        });
    });


    /**
     * Responde a la petición GET para publicar una nueva oferta
     */
    app.get("/offers/add", function (req, res) {
        let response = {
            email: req.session.user,
            amount: req.session.userAmount
        }
        appLogger.createNewLog("El usuario " + req.session.user + " ha intentado publicar una oferta", "PET");
        res.render("offers/add.twig", response);
    });

    /**
     * Responde a la petición POST para publicar una nueva oferta
     */
    app.post("/offers/add", function (req, res) {
        if (checkEmpty(req.body.title)) {
            appLogger.createNewLog("El usuario " + req.session.user + " ha intentado publicar una oferta sin título", "PET-ERR");
            res.redirect("/offers/add" +
                "?message=Se ha producido un error al añadir la oferta, título vacío " +
                "&messageType=alert-danger ");
        } else if (checkEmpty(req.body.description)) {
            appLogger.createNewLog("El usuario " + req.session.user + " ha intentado publicar una oferta sin descripción", "PET-ERR");
            res.redirect("/offers/add" +
                "?message=Se ha producido un error al añadir la oferta, detalles vacíos " +
                "&messageType=alert-danger ");
        } else if (checkInvalidPrice(req.body.price)) {
            appLogger.createNewLog("El usuario " + req.session.user + " ha intentado publicar una oferta con un precio no válido", "PET-ERR");
            res.redirect("/offers/add" +
                "?message=Se ha producido un error al añadir la oferta, precio no válido" +
                "&messageType=alert-danger ");
        } else {
            let offer = {
                title: req.body.title,
                description: req.body.description,
                price: req.body.price,
                date: new Date().toLocaleDateString(),
                seller: req.session.user,
                buyer: null,
                sold: false
            }

            // si se marca como destacada, comprueba que el saldo sea al menos 20 euros
            if (req.body.highlight && req.session.userAmount < 20) {
                appLogger.createNewLog("El usuario " + req.session.user + " ha intentado publicar una oferta destacada sin saldo suficiente", "PET-ERR");
                res.redirect("/offers/add" +
                    "?message=Se ha producido un error al añadir la oferta, insuficiente saldo para destacar oferta " +
                    "&messageType=alert-danger ");
            } else {
                offer.highlight = !!req.body.highlight;
                // inserta la oferta
                offersRepository.insertOffer(offer).then(offerId => {
                    // si se marca como destacada, reduce el saldo en 20 euros
                    if (req.body.highlight) {
                        let newUser = {amount: req.session.userAmount - 20};
                        let filter = {email: req.session.user};
                        // que no se cree un documento nuevo, si no existe
                        let options = {upsert: false};
                        usersRepository.updateUser(newUser, filter, options).then(() => {
                            req.session.userAmount = newUser.amount;
                            appLogger.createNewLog("El usuario " + req.session.user + " ha publicado una oferta destacada", "PET");
                            res.redirect("/publications");
                        }).catch(error => {
                            appLogger.createNewLog("Se ha producido un error al actualizar el saldo del usuario " + req.session.user, "PET-ERR");
                            res.send("Error al actualizar el usuario " + error);
                        });
                    } else {
                        appLogger.createNewLog("El usuario " + req.session.user + " ha publicado una oferta", "PET");
                        res.redirect("/publications");
                    }
                }).catch(error => {
                    appLogger.createNewLog("Se ha producido un error al publicar la oferta del usuario " + req.session.user, "PET-ERR");
                    res.redirect("/offers/add" +
                        "?message=Se ha producido un error al publicar la oferta." +
                        "&messageType=alert-danger ");
                });
            }
        }
    });

    /**
     * Responde a la petición GET para eliminar una canción según el ID
     * especificado en la URL
     */
    app.get("/offers/delete/:id", function (req, res) {
        let filter = {_id: ObjectId(req.params.id)};
        offersRepository.findOffer(filter, {}).then(offer => {
            // si no se vendio
            if (!offer.sold) {
                // y si es del usuario logeado
                let loggedUserEmail = offer.seller;
                if (loggedUserEmail === req.session.user) {
                    // borrar
                    offersRepository.deleteOffer(filter, {}).then(result => {
                        appLogger.createNewLog("El usuario " + req.session.user + " ha borrado una oferta", "PET");
                        res.redirect("/publications");
                    }).catch(error => {
                        appLogger.createNewLog("Se ha producido un error al borrar la oferta del usuario " + req.session.user, "PET-ERR");
                        res.redirect("/publications" +
                            "?message=Se ha producido un error al borrar la oferta." +
                            "&messageType=alert-danger ");
                    });
                } else {
                    appLogger.createNewLog("El usuario " + req.session.user + " ha intentado borrar una oferta que no es suya", "PET-ERR");
                    res.redirect("/publications" +
                        "?message=Acceso denegado." +
                        "&messageType=alert-danger ");
                }
            } else {
                appLogger.createNewLog("El usuario " + req.session.user + " ha intentado borrar una oferta que ya se vendió", "PET-ERR");
                res.redirect("/publications" +
                    "?message=Ya se ha vendido." +
                    "&messageType=alert-danger ");
            }
        }).catch(error => {
            appLogger.createNewLog("Se ha producido un error al recuperar la oferta del usuario " + req.session.user, "PET-ERR");
            res.redirect("/publications" +
                "?message=Se ha producido un error al recuperar la oferta." +
                "&messageType=alert-danger ");
        });
    });

    /**
     * Responde a la petición GET cuando quiere ver todas las ofertas en la vista de shop
     */
    app.get('/shop', function (req, res) {
        let filter = {};
        let options = {sort: {title: 1}};

        if (req.query.search != null && typeof (req.query.search) != "undefined" && req.query.search != "") {
            filter = {"title": {$regex: new RegExp(".*" + req.query.search + ".*", "i")}};
        }

        let page = parseInt(req.query.page); // Es String !!!
        if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === "0") {
            //Puede no venir el param
            page = 1;
        }
        offersRepository.getOffersPg(filter, options, page, 5).then(result => {
            let lastPage = result.total / 5;
            if (result.total % 5 > 0) { // Sobran decimales
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
                offers: result.offers,
                pages: pages,
                currentPage: page,
            }
            // Sacar destacadas
            let filterH = {highlight: true};
            offersRepository.getOffers(filterH, {}).then(offersH => {
                response.offersH = offersH;
                appLogger.createNewLog("El usuario " + req.session.user + " ha accedido a la tienda", "PET");
                res.render("shop.twig", response);
            });
        }).catch(error => {
            appLogger.createNewLog("Se ha producido un error al listar las ofertas " + req.session.user, "PET-ERR");
            res.send("Se ha producido un error al listar las ofertas " + error)
        });
    });

    /**
     * Responde a la petición GET para comprar una oferta
     */
    app.get('/offers/buy/:id', function (req, res) {
        let offerId = ObjectId(req.params.id);
        let shop = {
            user: req.session.user,
            offerId: offerId
        }

        checkCanAffordOffer(shop.user, offerId, function (canAffordIt) {
            checkOwnOffer(shop.user, offerId, function (notOwnOffer) {
                if (notOwnOffer && canAffordIt) {
                    offersRepository.buyOffer(shop, function (shopId) {
                        if (shopId == null) {
                            appLogger.createNewLog("Se ha producido un error al comprar la oferta " + req.session.user, "PET-ERR");
                            res.redirect("/shop" +
                                "?message=Error a la hora de comprar una oferta" +
                                "&messageType=alert-danger ");
                        }
                        let newOffer = {sold: true, buyer: shop.user};
                        let filter = {_id: ObjectId(req.params.id)};
                        let options = {upsert: false};
                        offersRepository.updateOffer(newOffer, filter, options).then(() => {
                            removeFromUserAmount(req, res, offerId, function (isCorrect) {
                                if (isCorrect) {
                                    appLogger.createNewLog("El usuario " + req.session.user + " ha comprado una oferta", "PET");
                                    res.redirect("/purchases");
                                } else {
                                    appLogger.createNewLog("Se ha producido un error al comprar la oferta " + req.session.user, "PET-ERR");
                                    res.send("Error")
                                }
                            })
                        }).catch(error => {
                            appLogger.createNewLog("Se ha producido un error al comprar la oferta " + req.session.user, "PET-ERR");
                            res.redirect("/shop" +
                                "?message=Error a la hora de comprar una oferta" + error +
                                "&messageType=alert-danger ");
                        })
                    })

                } else if (!notOwnOffer) {
                    appLogger.createNewLog("El usuario " + req.session.user + " ha intentado comprar una oferta que es suya", "PET-ERR");
                    res.redirect("/shop" +
                        "?message=Error comprar la oferta: Eres el vendedor no puedes comprarla" +
                        "&messageType=alert-danger ");
                } else if (!canAffordIt) {
                    appLogger.createNewLog("El usuario " + req.session.user + " ha intentado comprar una oferta que no puede permitirse", "PET-ERR");
                    res.redirect("/shop" +
                        "?message=Error comprar la oferta: dinero insuficiente" +
                        "&messageType=alert-danger ");
                } else {
                    appLogger.createNewLog("Se ha producido un error al comprar la oferta " + req.session.user, "PET-ERR");
                    res.redirect("/purchases");
                }

            });
        })

    });

    function removeFromUserAmount(req, res, offerId, callBackFunc) {
        let filter = {email: req.session.user};
        let filterOffer = {"_id": offerId};
        let options = {upsert: false};
        offersRepository.findOffer(filterOffer, {}).then(offer => {
            let newUser = {amount: req.session.userAmount - offer.price};
            usersRepository.updateUser(newUser, filter, options).then(() => {
                req.session.userAmount = newUser.amount;
                callBackFunc(true);
            }).catch(error => {
                callBackFunc(false);
            })

        }).catch(error => {
            callBackFunc(false);
        })
    }

    function checkCanAffordOffer(user, offerId, callBackFunc) {
        let filterOffer = {"_id": offerId};
        let options = {}

        offersRepository.findOffer(filterOffer, options).then(offer => {
            if (offer === null) {
                callBackFunc(false)
            } else if (user.userAmount < offer.amount) {
                callBackFunc(false)
            } else {
                callBackFunc(true)
            }
        }).catch(err => {
            callBackFunc(false)
        })
    }

    /**
     * Función que mira si la oferta se podría comprar y mira si se esta comprando una oferta que ha sido publicada por si mismo
     * además de comprobar si ya está comrpado o no
     * @param user
     * @param offerId
     * @param callBackFunc
     */
    function checkOwnOffer(user, offerId, callBackFunc) {
        let filterOfferAuthor = {"_id": offerId, "seller": user}
        let filterBougthOffer = {"offerId": offerId, "user": user}
        let options = {}
        offersRepository.findOffer(filterOfferAuthor, options).then(offers => {
            if (offers === null) {
                callBackFunc(true)
            } else {
                offersRepository.getPurchases(filterBougthOffer, options).then(purchasedIds => {
                    if (purchasedIds === null || purchasedIds.length === 0) {
                        callBackFunc(true)
                    } else {
                        callBackFunc(true)
                    }
                }).catch(err => {
                        callBackFunc(false)
                    }
                );
            }
        }).catch(err => {
                callBackFunc(false)
            }
        )
    }

    // ___________________________________________________________________

    /**
     * Devuelve true si el valor especificado no es válido (está vacío o es null)
     * @param value
     * @returns {boolean}
     */
    function checkEmpty(value) {
        return value === "undefined" || value === null || value.toString().trim().length === 0;
    }

    /**
     * Devuelve true si el precio no es válido (no es un número o es negativo)
     * @param value
     * @returns {boolean}
     */
    function checkInvalidPrice(value) {
        if (isNaN(value) || value.toString().trim().length === 0)
            return true;
        return value < 0;
    }

};