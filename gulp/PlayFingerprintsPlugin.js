"use strict";

const crypto = require('crypto');
const fs = require('fs');

/**
 * Webpack plugin that generates some extra files for Play! Framework
 * to use to generate versioned assets.
 *
 * It calculates the MD5 hash of each source and then:
 * Adds a .md5 file containing that hash
 * Adds a HASH-FILENAME copy of the original file.
 *
 * However if it detects a filename that already has a hash, it will use that
 * hash and put it in the .md5 hash file (it doesn't matter if it is actually MD5).
 * This is useful as Webpack likes to generate its own hashes.
 *
 * Logic based on the gulp-play-assets module.
 */

function apply(options, compiler) {
  compiler.plugin('emit', (compilation, done) => {
    const assets = compilation.assets;
    const versionedFilenames = {};

    for (const filename in assets) {
      if (assets.hasOwnProperty(filename)) {

        const dynamicChunk = filename.match(/^([a-f0-9]{16,})-([\w\-]+.js(\.map)?)$/)
        if (dynamicChunk) {
          //console.log('Found dynamic chunk', dynamicChunk);
          // This is a dynamic chunk that uses [chunkhash]
          // in its filename already - so reverse engineer the .md5 file
          // to allow the Gulp script to find the fingerprinted version.
          const hash = dynamicChunk[1];
          const name = dynamicChunk[2];
          assets[`${name}.md5`] = {
            source: () => hash,
            size: () => hash.length,
          };

          // don't really need this, but Play seems not to serve the file
          // unless the non-fingerprinted version exists.
          assets[`${name}`] = assets[filename];
          continue;
        }

        const hash = crypto.createHash("md5");
        hash.update(assets[filename].source());
        const md5 = hash.digest('hex');

        // Identical to original file but with hash prepended.
        assets[`${md5}-${filename}`] = assets[filename];

        // Fingerprint .md5 file
        assets[`${filename}.md5`] = {
          source: () => md5,
          size: () => md5.length,
        };

        versionedFilenames[filename] = `${md5}-${filename}`;
      }
    };

    // for (const chunkId in compilation.chunks) {
    //   if (compilation.chunks.hasOwnProperty(chunkId)) {
    //     const chunk = compilation.chunks[chunkId];
    //     chunk.files = chunk.files.map( file => {
    //       console.log(`Replacing ${file} with ${versionedFilenames[file]}`);
    //       return versionedFilenames[file];
    //     });
    //   }
    // }

    done();
  });
};

const PlayFingerprintsPlugin = function(options) {
  // easy access to options from apply()
  this.apply = apply.bind(this, options || {});
};



module.exports = PlayFingerprintsPlugin;
