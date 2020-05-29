#!/bin/sh

# exit if any command exits with a non zero error code
set -e

# define which version you want to load (needs to be any of the tags in the github repo: https://github.com/swagger-api/swagger-ui/releases)
SWAGGER_UI_VERSION=v3.25.5

# load the repo content
wget -O "$SWAGGER_UI_VERSION.tar.gz" "https://github.com/swagger-api/swagger-ui/archive/$SWAGGER_UI_VERSION.tar.gz"

# create a new temporary directory
TMP_DIR=./tmp/swagger-download
mkdir -p $TMP_DIR

# extract
tar -xf $SWAGGER_UI_VERSION.tar.gz -C $TMP_DIR

# destination folder is within the resources folder of the swagger-ui module
DEST_FOLDER=./swagger-ui/src/main/resources/swagger-ui

mkdir -p $DEST_FOLDER
rm -rf $DEST_FOLDER/*

# get the 'dist' folder of the github repo
SOURCE_FOLDER="$TMP_DIR/$(ls $TMP_DIR | head -n 1)/dist/*"

# copy the content of the distribution folder
cp -R $SOURCE_FOLDER $DEST_FOLDER

# clean up
rm -rf $TMP_DIR

rm "$SWAGGER_UI_VERSION.tar.gz"
