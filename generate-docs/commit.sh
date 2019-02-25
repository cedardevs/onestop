VERSION=$1
DIR=../docs/api/$VERSION/
git checkout gh-pages
rm -rf $DIR
mkdir -p $DIR
cp -r build/docs/api/ $DIR
cp -r build/docs/schema $DIR
git add ../docs
git commit -m "Updated generated docs with version '${VERSION}'."
git push -q https://${GITHUB_TOKEN}@github.com/cedardevs/onestop.git gh-pages
