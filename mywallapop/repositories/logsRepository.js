module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    },

    /**
     * Inserta un log en la base de datos
     * @param log
     * @returns {Promise<*>}
     */
    insertLog: async function (log) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'logs';
            const logsCollection = database.collection(collectionName);
            const result = await logsCollection.insertOne(log);
            return result.insertedId;
        } catch (error) {
            throw (error);
        }
    },
    getLogs: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'logs';
            const logsCollection = database.collection(collectionName);
            return await logsCollection.find(filter, options).toArray();
        } catch (error) {
            throw (error);
        }
    },
    deleteLogs: async function (filter) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'logs';
            const logsCollection = database.collection(collectionName);
            return await logsCollection.deleteMany(filter);
        } catch (error) {
            throw (error);
        }
    }
}