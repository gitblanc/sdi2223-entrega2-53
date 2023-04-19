const {ObjectId} = require("mongodb");
module.exports = function (app, offersRepository, usersRepository) {

    /**
     * Responde a la petición GET para ver las ofertas
     * publicadas por el usuario logeado
     */
    app.get("/publications", function (req, res){
        let numItemsPorPagina = 4;

        // obtiene el id del usuario
        usersRepository.findUser({email: req.session.user}).then(user => {
            let filter = {seller: user._id };
            let page = parseInt(req.query.page);
            if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === 0)
                page = 1;

            // obtiene las ofertas
            offersRepository.getOffers(filter, {}).then(result => {
                let lastPage = result.total / numItemsPorPagina;
                if (result.total % numItemsPorPagina > 0) {
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
                    itemsPerPage: numItemsPorPagina
                }
                res.render("offers/myOffersList.twig", response);
            }).catch(error => {
                res.send("Se ha producido un error al listar las publicaciones " + error)
            });
        }).catch(error => {
            res.send("Se ha producido un error al recuperar el usuario " + error)
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
            // obtiene el id del usuario logeado
            usersRepository.findUser({email: req.session.user}, {}).then(user => {
                let offer = {
                    title: req.body.title,
                    description: req.body.description,
                    price: req.body.price,
                    date: new Date().toLocaleDateString(),
                    seller: user._id,
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

            }).catch(error => {
                res.redirect("/offers/add" +
                    "?message=Se ha producido un error al obtener el usuario." +
                    "&messageType=alert-danger ");
            });
        }
    });

    app.get("/offers/delete/:id", function(req, res){
        let filter = {_id: ObjectId(req.params.id)};
        offersRepository.findOffer(filter, {}).then( offer => {
            // si no se vendio
            if(!offer.sold)
                usersRepository.findUser({_id: offer.seller}, {}).then(user => {
                    // y si es del usuario logeado
                    let loggedUserEmail = user.email;
                    if (loggedUserEmail === req.session.user) {
                        // borrar
                        offersRepository.deleteOffer(filter, {}).then(result => {
                            res.redirect("/publications");
                        }).catch(error => {
                            res.redirect("/publications" +
                                "?message=Se ha producido un error al borrar la oferta." +
                                "&messageType=alert-danger ");
                        });
                    }
                });
        }).catch(error => {
            res.redirect("/publications" +
                "?message=Se ha producido un error al recuperar la oferta." +
                "&messageType=alert-danger ");
        });
    });

    // ___________________________________________________________________

    function checkEmpty(value){
        return value === "undefined" || value === null || value.toString().trim().length === 0;
    }

    function checkInvalidPrice(value){
        if(isNaN(value) || value.toString().trim().length === 0)
            return true;
        return value < 0;
    }

};