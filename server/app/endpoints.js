// Import config
const config  = require(process.env.DD_CONFIG || '/etc/dd_conf/config.json');
const rand    = require('./rand');

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
            var userName;
            if (users[socId]) userName = users[socId].name;

            // Get the ID label for the message
            var attr = socId + ((userName != undefined) ? " (" + userName + ")" : "");

            // Log the message
            console.log(`[${attr}] ${message}`);
        }
    }

    function addGame(gameId) {

    }

    function joinGame(gameId, socket) {
        // Return false if the game does not exist
        if (game[gameId] == undefined) return false;
        // Join the room corrosponsind to the game ID
        socket.join(gameId);
        // Add the user to the room
        games[gameId].users[socket.id] = users[socket.id];
        return true;
    }

    /**
     * Handler on a socket connection being a established
     */
    io.on('connection', socket => {

        log(socket.id, "User connected");

        /**
         * Updates a client's profile
         */
        socket.on('update profile', (profile) => {
            log(socket.id, `Profile update request. Name: ${profile.name}, Image: ${profile.imgUrl}`);
            // Initialize a user if not seen before
            if (!users[socket.id]) {
                users[socket.id] = {};
            }
            // Update user name
            if (profile.name) {
                users[socket.id].name = profile.name;
            }
            // Update user image
            if (profile.imgUrl) {
                users[socket.id].imgUrl = profile.imgUrl;
            }
        });

        /**
         * Join game request
         */
        socket.on('join game', (gameId, cb) => {
            log(socket.id, `Game join request. Game ID: ${gameId}`);
            
            // Attempt to join the game
            cb(joinGame(gameId));
        });

        /**
         * Host game request
         */
        socket.on('host game', (cb) => {
            log(socket.id, `Host game request`);
            
            // Generate a Game ID
            var gId = rand.genereteRandomId(config.GAME_ID_LENGTH);
            while (games[gId] != undefined) {
                gId = rand.genereteRandomId(config.GAME_ID_LENGTH);
            }

            // Setup the game
            games[gId] = {
                users = {},
                objects = []
            };

            // Join the game
            joinGame(gId);

            // Return the game ID
            cb(gId);
        });

        /**
         * Leave game request
         */
        socket.on('leave game', () => {
            log(socket.id, `Game leave request`);
        });

        /**
         * Disconnection handler
         */
        socket.on('disconnect', () => {
            // TODO: Handle disconnect
        })

    }); 

}