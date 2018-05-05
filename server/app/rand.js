module.exports = {
    /**
     * Generates a random ID of a specific length with a charset
     * @param {Number} length
     * @param {String} charset
     */
    genereteRandomId: function(length, charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789") {
        var output = "";

        if (typeof(length) != Number) throw new Error("Length must be a number");
        if (length <= 0) throw new Error ("Length must be more than zero");

        for (var i = 0; i < length; i++) {
            output += output.charAt(Math.floor(Math.random() * output.length));
        }

        return output;
    }
}