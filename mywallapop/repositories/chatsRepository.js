module.exports = {
    mongoClient: null,
    app: null,
    init: function (app, mongoClient) {
        this.mongoClient = mongoClient;
        this.app = app;
    },

    findChat: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'chats';
            const chatsCollection = database.collection(collectionName);
            return await chatsCollection.findOne(filter, options);
        } catch (error) {
            throw (error);
        }
    },

    getChats: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'chats';
            const chatsCollection = database.collection(collectionName);
            return await chatsCollection.find(filter, options).toArray();
        } catch (error) {
            throw (error);
        }
    },

    insertChat: async function (chat) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'chats';
            const chatsCollection = database.collection(collectionName);
            const result = await chatsCollection.insertOne(chat);
            return result.insertedId;
        } catch (error) {
            throw (error);
        }
    },

    deleteChats: async function (filter, options) {
        try {
            const client = await this.mongoClient.connect(this.app.get('connectionStrings'));
            const database = client.db("myWallapop");
            const collectionName = 'chats';
            const chatsCollection = database.collection(collectionName);
            const result = await chatsCollection.deleteMany(filter, options);
            return result;
        } catch (error) {
            throw (error);
        }
    }
};