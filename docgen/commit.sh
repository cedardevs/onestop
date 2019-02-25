git checkout gh-pages
rm -rf ../docs/api
rm -rf ../docs/schema
mkdir -p ../docs/api
mkdir -p ../docs/api/schema
mv build/docs/api ../docs/
mv build/docs/schema ../docs/api/
git add ../docs
# git commit -m "Updating generated api docs"
# git push
