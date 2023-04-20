const {ObjectId} = require("mongodb");
module.exports = function (app, offersRepository, usersRepository) {

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
            offersRepository.getOffers(filter, options).then(purchasedOffers => {
                let response = {
                    email:req.session.user,
                    amount:req.session.userAmount,
                    offers: purchasedOffers.offers
                }
                res.render("purchase.twig", response);
            }).catch(error => {
                res.send("Se ha producido un error al listar las ofertas compradas del usuario: " + error)
            });
        }).catch(error => {
            res.send("Se ha producido un error al listar las compras del usuario " + error)
        });
    });

    /**
     * Responde a la petición GET para ver las ofertas
     * publicadas por el usuario logeado
     */
    app.get("/publications", function (req, res){
        let numItemsPorPagina = 4;

        let filter = {seller: req.session.user };
        let page = parseInt(req.query.page);
        if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === 0)
            page = 1;

        // obtiene las ofertas
        offersRepository.getOffers(filter, {}, page).then(result => {
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
            res.render("offers/myOffersList.twig", response);
        }).catch(error => {
            res.send("Se ha producido un error al listar las publicaciones " + error)
        });
    });

    /**
     * Responde a la petición GET para ver las ofertas disponibles
     * para comprar
     */
    app.get("/offers/list", function (req, res) {
        let response = {
            email:req.session.user,
            amount:req.session.userAmount
        }
        res.render("offers/offerslist.twig",response);
    });

    /**
     * Responde a la petición GET para publicar una nueva oferta
     */
    app.get("/offers/add", function(req, res){
        let response = {
            email:req.session.user,
            amount:req.session.userAmount
        }
        res.render("offers/add.twig",response);
    });

    /**
     * Responde a la petición POST para publicar una nueva oferta
     */
    app.post("/offers/add", function(req, res){
        if(checkEmpty(req.body.title))
            res.redirect("/offers/add" +
                "?message=Se ha producido un error al añadir la oferta, título vacío " +
                "&messageType=alert-danger ");

        else if(checkEmpty(req.body.description))
            res.redirect("/offers/add" +
                "?message=Se ha producido un error al añadir la oferta, detalles vacíos " +
                "&messageType=alert-danger ");

        else if(checkInvalidPrice(req.body.price))
            res.redirect("/offers/add" +
                "?message=Se ha producido un error al añadir la oferta, precio no válido" +
                "&messageType=alert-danger ");

        else {
            let offer = {
                title: req.body.title,
                description: req.body.description,
                price: req.body.price,
                date: new Date().toLocaleDateString(),
                seller: req.session.user,
                buyer: null,
                sold: false
            }

            // inserta la oferta
            offersRepository.insertOffer(offer).then(offerId => {
                let response = {
                    email:req.session.user,
                    amount:req.session.userAmount
                }
                res.redirect("/publications");
            }).catch(error => {
                res.redirect("/offers/add" +
                    "?message=Se ha producido un error al publicar la oferta." +
                    "&messageType=alert-danger ");
            });
        }
    });

    /**
     * Responde a la petición GET para eliminar una canción según el ID
     * especificado en la URL
     */
    app.get("/offers/delete/:id", function(req, res){
        let filter = {_id: ObjectId(req.params.id)};
        offersRepository.findOffer(filter, {}).then( offer => {
            // si no se vendio
            if(!offer.sold) {
                // y si es del usuario logeado
                let loggedUserEmail = offer.seller;
                if (loggedUserEmail === req.session.user) {
                    // borrar
                    offersRepository.deleteOffer(filter, {}).then(result => {
                        res.redirect("/publications");
                    }).catch(error => {
                        res.redirect("/publications" +
                            "?message=Se ha producido un error al borrar la oferta." +
                            "&messageType=alert-danger ");
                    });
                }else{
                    res.redirect("/publications" +
                        "?message=Acceso denegado." +
                        "&messageType=alert-danger ");
                }
            }
            else{
                res.redirect("/publications" +
                    "?message=Ya se ha vendido." +
                    "&messageType=alert-danger ");
            }
        }).catch(error => {
            res.redirect("/publications" +
                "?message=Se ha producido un error al recuperar la oferta." +
                "&messageType=alert-danger ");
        });
    });

    // ___________________________________________________________________

    /**
     * Devuelve true si el valor especificado no es válido (está vacío o es null)
     * @param value
     * @returns {boolean}
     */
    function checkEmpty(value){
        return value === "undefined" || value === null || value.toString().trim().length === 0;
    }

    /**
     * Devuelve true si el precio no es válido (no es un número o es negativo)
     * @param value
     * @returns {boolean}
     */
    function checkInvalidPrice(value){
        if(isNaN(value) || value.toString().trim().length === 0)
            return true;
        return value < 0;
    }

};