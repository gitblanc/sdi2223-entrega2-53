const {ObjectId} = require("mongodb");
module.exports = function (app) {
    app.get("/offers/list", function (req, res) {
        let response = {
            email:req.session.user,
            amount:req.session.userAmount
        }
        res.render("offers/offerslist.twig",response);
    });

}
;