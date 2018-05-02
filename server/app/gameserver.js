/**
 * gameserver.js - Game Server Factory
 */

const express = require('express');
const app     = express();
const sio     = require('socket.io')

class GameServer {

    /**
     * GameServer constructor
     * @param {Object} opts 
     */
    constructor (opts) {
        if (!!opts.debug) {

            // Start in debug mode
            this.debug = true;

            // Create server
            this.server = require('http').createServer(app);

        } else {

            // Start as production
            this.debug = false;

            // Create server
            if (!opts.https) throw new Error('HTTPS required for production');
            this.server = require('https').createServer(opts.https);

        }

        this.io = sio(this.server);
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
function createGameServer(opts) {
    return new GameServer(opts);
}

// Expose createGameServer to other modules
module.exports = { createGameServer: createGameServer };