/**
 * ==================================================================
 *      ____                  __          __   ____             __  
 *     / __ \____  ____  ____/ /___  ____/ /  / __ \____ ______/ /_ 
 *    / / / / __ \/ __ \/ __  / __ `/ __  /  / / / / __ `/ ___/ __ \
 *   / /_/ / /_/ / /_/ / /_/ / /_/ / /_/ /  / /_/ / /_/ (__  ) / / /
 *  /_____/\____/\____/\__,_/\__,_/\__,_/  /_____/\__,_/____/_/ /_/ 
 *                                                               
 * ==================================================================
 *    Doodad Dash ~ v1.0.0 ~ (c) Adam Hirst 2018 ~ adam@ahirst.com
 * ==================================================================
 */

// Internal debug flag
const DEBUG = true;

// Dependencies
const fs = require('fs');
const GameServerFactory = require('./app/gameserver');

// HTTPS credentials
var httpsCredentials;
if (!!process.env.DDD_KEY && !!process.env.DDD_KEY) {
    httpsCredentials = {
        key: fs.readFileSync(process.env.DDD_KEY),
        cert: fs.readFileSync(process.env.DDD_CERT),
    }
}

// Create the game server
const gs = GameServerFactory.createGameServer({
    debug: DEBUG,
    https: httpsCredentials
});

// Listen on port 3000
gs.listen(3000, () => {
    console.log('Listening on 3000');
});