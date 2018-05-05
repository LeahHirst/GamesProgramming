// Import config
const config  = require(process.env.DD_CONFIG || '/etc/dd_conf/config.json');

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
        socket.on('join game', (gameId) => {
            log(socket.id, `Game join request. Game ID: ${gameId}`);
            socket.join(gameId);
            
            // Check if the joined room is empty
        });

        /**
         * Host game request
         */
        socket.on('host game', () => {
            log(socket.id, `Host game request`);
            
            // TODO: Generate room ID, join room.
        });

        /**
         * Disconnection handler
         */
        socket.on('disconnect', () => {
            // TODO: Handle disconnect
        })

    }); 

}