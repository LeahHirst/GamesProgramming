/**
 * gameserver.js - Game Server Factory
 */

const express = require('express');
const app     = express();
const sio     = require('socket.io');
const config  = require(process.env.DD_CONFIG || '/etc/dd_conf/config.json');

app.use(express.static('public'));

/**
 * Game Server class holds global game states
 */
class GameServer {

    /**
     * GameServer constructor
     */
    constructor () {
        if (config.DEBUG) {
            // Start in debug mode
            this.debug = true;

            // Create server
            this.server = require('http').createServer(app);
        } else {
            // Start as production
            this.debug = false;

            // Create server
            if (!config.HTTPS) throw new Error('HTTPS required for production');
            this.server = require('https').createServer(config.HTTPS);
        }

        // Initiate a socket.io instance
        this.io = sio(this.server);

        // Setup socket.io endpoints
        require('./endpoints')(this.io);
    }

    /**
     * Starts listening on the given port
     * @param {number} port 
     * @param {function} cb 
     */
    listen(port, cb) {
        this.server.listen(port, cb);
    }

}

/**
 * Creates a new instance of a GameServer.
 * @param {Object} opts 
 */
function createGameServer() {
    return new GameServer();
}

// Expose createGameServer to other modules
module.exports = { createGameServer: createGameServer };