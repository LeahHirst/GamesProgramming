// Import config
const config   = require(process.env.DD_CONFIG || '/etc/dd_conf/config.json');
const rand     = require('./rand');

// Export func
module.exports = (io) => { 

    /**
     * Stores client information
     */
    var users = {
        /**
         * Populated at runtime. Example format:
         * [Socket ID]: {
         *    name: [Person's name],
         *    imgUrl: [Image URL]
         * }
         */
    };

    /**
     * Dictionary of game IDs to games
     */
    var games = {
        
    };

    /**
     * Logs a message to the console
     * @param {String} socId 
     * @param {String} message 
     */
    function log(socId, message) {
        if (config.DEBUG_LOGGING) {
            // Get the user's name (if applicable)
            var userName = getUserName(socId);

            // Get the ID label for the message
            var attr = socId + ((userName != undefined) ? " (" + userName + ")" : "");

            // Log the message
            console.log(`[${attr}] ${message}`);
        }
    }

    /**
     * Gets a user's name
     * @param {String} socketId 
     */
    function getUserName(socketId) {
        var userName;
        if (users[socketId]) userName = users[socketId].name;
        return userName;
    }

    /**
     * Returns true if a given user is in a given game
     * @param {WebSocket} socket 
     * @param {String} gameId 
     */
    function isUserInGame(socket, gameId) {
        return users[socket.id].gameId != undefined;
    }

    /**
     * Creates a new game instance and returns the ID
     */
    function createGame() {
        // Generate a random ID for the game
        var gId = rand.genereteRandomId(config.GAME_ID_LENGTH);
        while (games[gId] != undefined) {
            gId = rand.genereteRandomId(config.GAME_ID_LENGTH);
        }

        // Setup the game
        games[gId] = {
            users: {},
            objects: []
        };

        // Return the ID
        return gId;
    }

    /**
     * Joins a game
     * @param {String} gameId 
     * @param {WebSocket} socket 
     */
    function joinGame(gameId, socket) {
        // Return false if the game does not exist
        if (games[gameId] == undefined) return false;
        if (isUserInGame(socket, gameId)) return false;
        // Check if the user has updated their profile
        if (users[socket.id] == undefined) return false;
        // Join the room corrosponsind to the game ID
        socket.join(gameId);
        // Add the user to the room
        games[gameId].users[socket.id] = users[socket.id];
        users[socket.id].gameId = gameId;
        emitUserListUpdate(gameId, `${getUserName(socket.id)} has joined the game`);
        return true;
    }

    /**
     * Removes a user from a game
     * @param {WebSocket} socket 
     */
    function leaveGame(socket) {
        // Find the game ID
        if (users[socket.id] == undefined) return false;
        var gameId = users[socket.id].gameId;
        users[socket.id].gameId = undefined;
        if (games[gameId] == undefined) return false;
        games[gameId].users[socket.id] == undefined;
        emitUserListUpdate(gameId, `${getUserName(socket.id)} has left the game`);
    }

    /**
     * Emits a user list update to clients in a game
     * @param {String} gameId 
     * @param {String} reason 
     */
    function emitUserListUpdate(gameId, reason) {
        var emittedBundle = {
            users: [],
            updateMessage: reason
        }

        // Populate the users array
        if (games[gameId] != undefined) {
            Object.keys(games[gameId].users).forEach(element => {
                if (games[gameId].users[element] != undefined) {
                    emittedBundle.users.push(games[gameId].users[element]);
                }
            });
        }

        io.to(gameId).emit('userlist update', emittedBundle);
    }



    /**
     * Handler on a socket connection being a established
     */
    io.on('connection', socket => {

        log(socket.id, "User connected");

        /**
         * Updates a client's profile
         */
        socket.on('update profile', (name, imgUrl) => {
            log(socket.id, `Profile update request. Name: ${name}, Image: ${imgUrl}`);
            // Initialize a user if not seen before
            if (!users[socket.id]) {
                users[socket.id] = {};
            }
            // Update user name
            if (name) {
                users[socket.id].name = name;
            }
            // Update user image
            if (imgUrl) {
                users[socket.id].imgUrl = imgUrl;
            }
        });

        /**
         * Join game request
         */
        socket.on('join game', (gameId, cb) => {
            log(socket.id, `Game join request. Game ID: ${gameId}`);
            
            // Attempt to join the game
            cb(joinGame(gameId, socket));
        });

        /**
         * Host game request
         */
        socket.on('host game', (cb) => {
            log(socket.id, `Host game request`);
            
            // Create a game
            var gId = createGame();

            // Join the game
            joinGame(gId, socket);

            // Return the game ID
            cb(gId);
        });

        /**
         * Leave game request
         */
        socket.on('leave game', (cb) => {
            log(socket.id, `Game leave request`);
            var res = leaveGame(socket);
            if (cb != undefined) {
                cb(res);
            }
        });

        /**
         * Disconnection handler
         */
        socket.on('disconnect', () => {
            log(socket.id, `User disconnected`)
            leaveGame(socket);
        })

    }); 

}