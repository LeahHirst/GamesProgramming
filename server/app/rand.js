module.exports = {
    /**
     * Generates a random ID of a specific length with a charset
     * @param {Number} length
     * @param {String} charset
     */
    genereteRandomId: function(length, charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789") {
        var output = "";

        if (typeof(length) != "number") throw new Error("Length must be a number");
        if (length <= 0) throw new Error ("Length must be more than zero");

        for (var i = 0; i < length; i++) {
            output += charset.charAt(Math.floor(Math.random() * charset.length));
        }

        return output;
    }
}