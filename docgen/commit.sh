# git checkout gh-pages
# rm -rf ../docs/api
# mkdir -p ../docs/api
# cp -r build/docs/api/ ../docs/api/
# cp -r build/docs/schema ../docs/api/
# git add ../docs
# git commit -m "Updating generated api docs"
# git push

VERSION=$1
DIR=../docs/api/$VERSION/
git checkout gh-pages
rm -rf $DIR
mkdir -p $DIR
cp -r build/docs/api/ $DIR
cp -r build/docs/schema $DIR
git add ../docs
