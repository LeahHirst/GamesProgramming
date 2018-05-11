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
            objects: [
                'laptop',
                'monitor',
                'pop bottle'
            ],
            userCount: 3
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
        games[gameId].userCount++;
        users[socket.id].gameId = gameId;
        emitUserListUpdate(gameId, `${getUserName(socket.id)} has joined the game`);
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
       console.log(games[gameId].objects);

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
                log(user.id, "Sending item " + games[gameId].objects[i]);
                games[gameId].users[user.id].score = 0;
                games[gameId].users[user.id].objects = rand.shuffleArray(games[gameId].objects);
    
                user.emit('object request', games[gameId].users[user.id].objects[i]);
                // Send out blank scoreboard
                scoreboardUpdate(gameId);
            }
        }
    }

    function getNext(socket) {
        var gameId = getGame(socket);
        games[gameId].users[socket.id].score++;
        scoreboardUpdate(gameId, socket);
        if (games[gameId].users[socket.id].score >= games[gameId].objects.length) {
            // User has won
            return undefined;
        } else {
            return games[gameId].users[socket.id].objects[games[gameId].users[socket.id].score];
        }
    }

    function scoreboardUpdate(gameId, socket) {

        // Determine if one of the top three players have changed (i.e. if socket was one of them)
        var users = Object.keys(games[gameId].users);
        var top1 = { score: -1 };
        var top2 = { score: -1 };
        var top3 = { score: -1 };
        for (var i = 0; i < users.length; i++) {
            console.log('Checking user ' + users[i]);
            if (games[gameId].users[users[i]] == undefined) continue;
            var userScore = games[gameId].users[users[i]].score;
            console.log(userScore);
            if (top1.score < userScore) {
                // Player is in first
                top1.user = users[i];
                top1.score = userScore;
            } else if (top2.score < userScore) {
                top2.user = users[i];
                top2.score = userScore;
            } else if (top3.score < userScore) {
                top3.user = users[i];
                top3.score = userScore;
            }
        }

        if (socket == undefined) {
            // Send scoreboard anyway
            var bundle = {
                users: []
            };
            var u1 = games[gameId].users[0];
            if (u1 != undefined) bundle.users.push(u1);
            var u2 = games[gameId].users[0];
            if (u2 != undefined) bundle.users.push(u2);
            var u3 = games[gameId].users[0];
            if (u3 != undefined) bundle.users.push(u3);
            io.to(gameId).emit('scoreboard update', bundle);
        } else {
            if (top1.user == socket.id || top2.user == socket.id || top3.user == socket.id) {
                log(socket.id, "Updating scoreboard");
                // Update the scoreboard
                var bundle = {
                    users: []
                };
                if (games[gameId].users[top1.user] != undefined) {
                    top1.user = games[gameId].users[top1.user];
                    bundle.users.push(top1.user);
                }
                if (games[gameId].users[top2.user] != undefined) {
                    top2.user = games[gameId].users[top2.user];
                    bundle.users.push(top2.user);
                }
                if (games[gameId].users[top3.user] != undefined) {
                    top3.user = games[gameId].users[top3.user];
                    bundle.users.push(top3.user);
                }
                io.to(gameId).emit('scoreboard update', bundle);
            }
        }
        
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
            log(socket.id, "User guessed " + object);
            if (games[gameId].users[socket.id].objects[games[gameId].users[socket.id].score] == object) {
                // Success!
                var next = getNext(socket);
                if (next == undefined) {
                    // TODO: User has won
                    next = "WIN!"
                } // else {
                socket.emit('object request', next);
                // }
            }
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