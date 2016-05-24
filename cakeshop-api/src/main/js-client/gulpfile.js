
var gulp        = require("gulp");
var gutil       = require("gulp-util");
var concatenate = require("gulp-concat");
var minify      = require("gulp-uglify");
var bower       = require("main-bower-files");
var watch       = require("gulp-watch");
var batch       = require("gulp-batch");
var plumber     = require("gulp-plumber");

gulp.task("default", ["compile"]);

gulp.task("compile", function() {

    var dest = "dist/";
    var libs = "lib/**/*.js";

    gulp.src(libs)
        .pipe(concatenate("client.js"))
        .pipe(gulp.dest(dest))
        .pipe(concatenate("client-min.js"))
        .pipe(minify())
        .pipe(gulp.dest(dest));

    gulp.src(bower().concat(libs))
        .pipe(concatenate("client-combined.js"))
        .pipe(gulp.dest(dest))
        .pipe(concatenate("client-combined-min.js"))
        .pipe(minify())
        .pipe(gulp.dest(dest));

});

gulp.task("install", ["compile"], function() {
    gulp.src("dist/client.js")
        .pipe(concatenate("blockchain-sdk-client.js"))
        .pipe(gulp.dest("../webapp/js/vendor/"));
});

gulp.task("watch", ["install"], function() {
    watch("lib/**/*.js", batch(function (events, done) {
        gutil.log("-- recompiling on file change --");
        gulp.start("install", done);
    }));
});
