const {ObjectId} = require("mongodb");
module.exports = function (app) {
    app.get("/offers/list", function (req, res) {
        res.render("offers/offerslist.twig");
    });

}
;