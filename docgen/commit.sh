git checkout gh-pages
rm -rf ../docs/api
rm -rf ../docs/schema
mkdir -p ../docs/api
mkdir -p ../docs/schema
mv build/docs/api ../docs/
mv build/docs/schema ../docs/
git add ../docs
# git commit -m "Updating generated api docs"
# git push
