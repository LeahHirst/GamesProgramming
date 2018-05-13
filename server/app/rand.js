
function generateRandomInt(max, min = 0) {
    if (typeof(max) != "number") throw new Error("Max must be a number");
    if (typeof(min) != "number") throw new Error("Min must be a number");
    if (min >= max) throw new Error("Min must be less than max");

    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function generateRandomId(length, charset = "0123456789") {
    var output = "";

    if (typeof(length) != "number") throw new Error("Length must be a number");
    if (length <= 0) throw new Error ("Length must be more than zero");

    for (var i = 0; i < length; i++) {
        output += charset.charAt(generateRandomInt(charset.length));
    }

    return output;
}

function shuffleArray(array) {
    for (var i = 0; i < array.length - 1; i++) {
        var swapIndex = generateRandomInt(array.length - 1, i);
        var tmp = array[i];
        array[i] = array[swapIndex];
        array[swapIndex] = tmp;
    }
    return array;
}


module.exports = {
    /**
     * Generates a random integer between min and max
     * @param {Number} max
     * @param {Number} min
     */
    generateRandomInt: generateRandomInt,
    /**
     * Generates a random ID of a specific length with a charset
     * @param {Number} length
     * @param {String} charset
     */
    genereteRandomId: generateRandomId,
    /**
     * Shuffles a given array
     * @param {Array} array
     */
    shuffleArray: shuffleArray
}