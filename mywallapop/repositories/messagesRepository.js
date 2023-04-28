module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    },
    /**
     * Añade un mensaje enviado por un usuario en la base de datos
     * @param message, el mensaje del usuario
     * @param callbackFunction
     */
    insertMessage: function (message, callbackFunction) {
        this.mongoClient.connect(this.app.get('connectionStrings'), function (err, dbClient) {
            if (err) {
                callbackFunction(null)
            } else {
                const database = dbClient.db("myWallapop");
                const collectionName = 'messages';
                const messagesCollection = database.collection(collectionName);
                messagesCollection.insertOne(message)
                    .then(result => callbackFunction(result.insertedId))
                    .then(() => dbClient.close())
                    .catch(err => callbackFunction({error: err.message}));
            }
        });
    },

    getMessages: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'messages';
            const messagesCollection = database.collection(collectionName);
            return await messagesCollection.find(filter, options).toArray();
        } catch (error) {
            throw (error);
        }
    }
};