const exec = require('child_process');
const fs = require('fs');

module.exports = {

    /**
     * Classifies an JPEG Base64 encoded image
     */
    classify: function(socId, image, cb) {
        if (!fs.existsSync('./temp'))
            fs.mkdirSync('./temp');
        fs.writeFileSync(`./temp/${socId}.jpg`, image);
        
        var shell = `source tensorflow/tutorials/image/imagenet/venv/bin/activate && python tensorflow/tutorials/image/imagenet/classify_image.py --image_file="${image}"`;

        exec(shell, (err, stdout, stderr) => {
            if (err) return cb(err);

            var re = /([a-zA-Z]*)[, [a-zA-Z]* \(score/gm;
            var object = re.exec(stdout)[1];
            
            cb(null, object);
        });

    }

}