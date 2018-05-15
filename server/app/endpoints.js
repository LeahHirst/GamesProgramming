// Import config
const config   = require(process.env.DD_CONFIG || '/etc/dd_conf/config.json');
const rand     = require('./rand');

Array.prototype.clean = function(deleteValue) {
    for (var i = 0; i < this.length; i++) {
        if (this[i] == deleteValue) {         
            this.splice(i, 1);
            i--;
        }
    }
    return this;
};

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

            var gameId = '';

            if (users[socId] != undefined)
                gameId = users[socId].gameId;

            // Get the ID label for the message
            var attr = socId + ((userName != undefined) ? " (" + userName + " [" + gameId + "])" : "");

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
        if (users[socket.id] == undefined) return false;
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
            objects: [],
            userCount: 0
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
        games[gameId].users[socket.id] = JSON.parse(JSON.stringify(users[socket.id]));
        games[gameId].userCount++;
        users[socket.id].gameId = gameId;
        
        setTimeout(() => {
            emitUserListUpdate(gameId, `${getUserName(socket.id)} has joined the game`);
        }, 500);
        
        return true;
    }

    /**
     * Called when a host clicks start from the lobby
     * @param {WebSocket} socket
     */
    function startPregame(socket) {
        var gameId = getGame(socket);
        if (gameId == undefined) return false;
        io.to(gameId).emit('start pregame');
    }

    /**
     * Skips an object
     * @param {WebSocket} socket 
     */
    function skip(socket) {
        var gameId = getGame(socket);
        if (gameId == undefined) return false;

        var currentObject = getCurrent(socket);
        games[gameId].users[socket.id].objects
            .splice(games[gameId].users[socket.id].score, 1);
        games[gameId].users[socket.id].objects.push(currentObject);

        currentObject = getCurrent(socket);
        socket.emit('object request', currentObject);
    }

    /**
     * Called when a user locks in an object
     * @param {WebSocket} socket
     * @param {String} object
     */
    function setObject(socket, object) {
       var gameId = getGame(socket);
       log(socket.id, "Setting object to " + object + " (game id: " + gameId + ")");
       if (gameId == undefined) return false;
       if (games[gameId].users[socket.id].lockedIn) return false;
       games[gameId].users[socket.id].lockedIn = true;
       games[gameId].objects.push(object);

       if (games[gameId].objects.length == games[gameId].userCount) {
           // Game ready to begin
           log(socket.id, 'Starting countdown');
           io.to(gameId).emit('start countdown');

           setTimeout(() => { sendObjects(gameId); }, 5500);
       }
    }

    /**
     * Send the first set of objects out to a room
     */
    function sendObjects(gameId) {
        // Send out the objects
        for (var i = 0; i < games[gameId].objects.length; i++) {
            var user = io.sockets.connected[Object.keys(games[gameId].users)[i]];
            if (user != undefined) {
                var object = games[gameId].objects[i];
                games[gameId].users[user.id].score = 0;

                var objects = JSON.parse(JSON.stringify(rand.shuffleArray(games[gameId].objects)));
                
                // Swap the first item with the starting object
                var swapIndex = objects.indexOf(object);
                var tmp = objects[0];
                objects[0] = objects[swapIndex];
                objects[swapIndex] = tmp;

                games[gameId].users[user.id].objects = objects;
                
                user.emit('object request', object);
            }
        }
        io.to(gameId).emit('scoreboard update', { users: [] });

        setTimeout(() => {
            if (games[gameId] != undefined) {
                io.to(gameId).emit('end game', {
                    users: getTopN(gameId, 3)
                });
                deleteGame(gameId);
            }
        }, 1000 * 60 * 3);
    }

    function getCurrent(socket) {
        var gameId = getGame(socket);
        if (!games[gameId] || !games[gameId].users[socket.id] || !games[gameId].objects) return undefined;
        if (!games[gameId].users[socket.id]) return undefined;
        if (games[gameId].users[socket.id].score >= games[gameId].objects.length) {
            // User has won
            return undefined;
        } else {
            return games[gameId].users[socket.id].objects[games[gameId].users[socket.id].score];
        }
    }

    function getNext(socket) {
        var gameId = getGame(socket);
        if (!games[gameId]) return undefined;
        if (!games[gameId].users[socket.id]) return undefined;
        games[gameId].users[socket.id].score++;
        scoreboardUpdate(gameId, socket);
        return getCurrent(socket);
    }

    /**
     * Gets the top n players (where score > 0)
     * @param {String} gameId 
     * @param {Number} n 
     */
    function getTopN(gameId, n) {
        // Get the users in game
        var users = Object.keys(games[gameId].users);
        // Holder for top n players
        var top = [];

        // Iterate through users in game
        for (var i = 0; i < users.length; i++) {
            var user = games[gameId].users[users[i]];
            if (user == undefined || user.score == 0) continue;
            
            // Check if this user is one of the top n
            for (var j = 0; j < n; j++) {
                if (top[j] == undefined || top[j].score < user.score) {
                    top.splice(j, 0, JSON.parse(JSON.stringify(user)));
                    top[j].objects = undefined;
                    top[j].gameId = undefined;
                    top[n+1] = undefined;
                    break;
                }
            }
        }
        
        return top.clean(null);
    }

    /**
     * Updates the scoreboard
     * @param {String} gameId 
     * @param {WebSocket} socket 
     */
    function scoreboardUpdate(gameId, socket) {
        // Update the scoreboard
        var bundle = {
            users: getTopN(gameId, 3)
        };

        io.to(gameId).emit('scoreboard update', bundle);        
    }

    /**
     * Returns the game of a user
     * @param {WebSocket} socket 
     */
    function getGame(socket) {
        if (users[socket.id] == undefined) return undefined;
        return users[socket.id].gameId;
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
        games[gameId].userCount--;
        emitUserListUpdate(gameId, `${getUserName(socket.id)} has left the game`);
    }

    /**
     * Deletes a game
     * @param {String} gameId 
     */
    function deleteGame(gameId) {
        io.to(gameId).emit('game deleted');
        games[gameId] = undefined;

        io.of('/').in(gameId).clients(function(error, clients) {
            if (clients.length > 0) {
                clients.forEach(function (socket_id) {
                    io.sockets.sockets[socket_id].leave(gameId);
                    users[socket_id].gameId = undefined;
                });
            }
        });
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
         * Set object request
         */
        socket.on('set object', (object) => {
            setObject(socket, object);
        });

        /**
         * Host start event
         */
        socket.on('start pregame', () => {
            startPregame(socket);
        });

        /**
         * Called when a user successfully detects an object
         */
        socket.on('item detected', (object) => {
            // Verify that object is the correct object to avoid duplicated calls etc.
            var gameId = getGame(socket);
            var current = getCurrent(socket);
            log(socket.id, `User guessed ${object}, actual answer was ${current}`);
            if (current == object) {
                // Success!
                var next = getNext(socket);
                if (next == undefined) {
                    // Use has won
                    io.to(gameId).emit('end game', {
                        users: getTopN(gameId, 3)
                    });
                    deleteGame(gameId);
                } else {
                    socket.emit('object request', next);
                }
            }
        });

        socket.on('skip', () => {
            skip(socket);
        })

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