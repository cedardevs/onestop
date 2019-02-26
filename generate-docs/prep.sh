set -e

mkdir -p build/src
rsync -av ../api-search/src/main/resources/api/ build/src/api
rsync -av ../api-search/src/main/resources/schema/ build/src/schema

sed -i '' -e "s/file:.*geo.json#/#/g" build/src/schema/components/geo.json

find build -type f | while read file
do
  echo "Fix 'file:' context in $file"
  sed -i '' -e "s#file:src/main/resources#.#g" $file
  sed -i '' '/$schema/d' $file
done
find build/src/schema -type f | while read file
do
  echo "Fix relative schema dir in $file"
  sed -i '' -e "s#./schema/##g" $file
done
find build/src/schema/components -type f | while read file
do
  echo "Fix relative components dir in $file"
  sed -i '' -e "s#components/##g" $file
done
