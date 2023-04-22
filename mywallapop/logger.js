const logsRepository = require('./repositories/logsRepository');

/**
 * Función que crea un log con su tipo, fecha y mensaje descriptivo
 * @param message
 * @param type
 * @returns {{date: number, type, message}}
 */
function createNewLog(message, type) {
    let log = {
        type: type,
        date: Date.now(),
        message: message
    }

    logsRepository.insertLog(log).then(id => {
        console.log("log:" + type + " - msg: " + message + " - id: " + id);
    })
}

module.exports = {
    createNewLog
}
