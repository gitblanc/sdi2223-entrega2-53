module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    },

    /**
     * Devuelve una lista de compras de la base de datos en base al filtro pasado como parámetro
     * @param filter filtro
     * @param options opciones
     * @returns {Promise<*>} lista de compras
     */
    getPurchases: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'purchases';
            const purchasesCollection = database.collection(collectionName);
            return await purchasesCollection.find(filter, options).toArray();
        } catch (error) {
            throw (error);
        }
    },

    findOffer: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const offer = await offersCollection.findOne(filter, options);
            return offer;
        } catch (error) {
            throw (error);
        }
    },
    buyOffer: function (shop, callbackFunction) {
        this.mongoClient.connect(this.app.get('connectionStrings'), function (err, dbClient) {
            if (err) {
                callbackFunction(null)
            } else {
                const database = dbClient.db("myWallapop");
                const collectionName = 'purchases';
                const purchasesCollection = database.collection(collectionName);
                purchasesCollection.insertOne(shop)
                    .then(result => callbackFunction(result.insertedId))
                    .then(() => dbClient.close())
                    .catch(err => callbackFunction({error: err.message}));
            }
        });
    },

    insertOffer: async function (offer) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const result = await offersCollection.insertOne(offer);
            return result.insertedId;
        } catch (error) {
            throw (error);
        }
    },
    insertOffers: async function (offer) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const result = await offersCollection.insertMany(offer);
            return result.insertedIds;
        } catch (error) {
            throw (error);
        }
    },
    updateSong: async function (offer, filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const songsCollection = database.collection(collectionName);
            const result = await songsCollection.updateOne(filter, {$set: offer}, options);
            return result;
        } catch (error) {
        }
    },

    getOffersPg: async function (filter, options, page, limit) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const cursor = offersCollection.find(filter, options).skip((page - 1) * limit).limit(limit)
            const offers = await cursor.toArray();
            const c = await offersCollection.find(filter, options).toArray();
            const result = {offers: offers, total: c.length};
            return result;
        } catch (error) {
            throw (error);
        }
    },

    getOffers: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            return await offersCollection.find(filter, options).toArray();
        } catch (error) {
            throw (error);
        }
    },

    deleteOffer: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const result = await offersCollection.deleteOne(filter, options);
            return result;
        } catch (error) {
            throw (error);
        }
    },
    deleteOffers: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            const result = await offersCollection.deleteMany(filter, options);
            return result;
        } catch (error) {
            throw (error);
        }
    },
    deletePurchases: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'purchases';
            const purchasesCollection = database.collection(collectionName);
            const result = await purchasesCollection.deleteMany(filter, options);
            return result;
        } catch (error) {
            throw (error);
        }
    },
    updateOffer: async function(newOffer, filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'offers';
            const offersCollection = database.collection(collectionName);
            return await offersCollection.updateOne(filter, {$set: newOffer}, options);
        } catch (error) {
            throw (error);
        }
    },
};