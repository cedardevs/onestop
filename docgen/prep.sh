
mkdir -p build/src
rsync -av ../api-search/src/main/resources/api/ build/src/api
rsync -av ../api-search/src/main/resources/schema/ build/src/schema

sed -i '' -e "s/file:.*geo.json#/#/g" build/src/schema/components/geo.json

find build -type f | while read file
do
  sed -i '' -e "s#file:src/main/resources#.#g" $file
  sed -i '' '/$schema/d' $file
done
find build/src/schema -type f | while read file
do
  sed -i '' -e "s#./schema/##g" $file
done
find build/src/schema/components -type f | while read file
do
  sed -i '' -e "s#components/##g" $file
done

# find build/src/api -type f | while read file
# do
#   echo $file
#   basedir=$(echo $file | sed -e "s#[a-zA-Z]*.yml##")
#   outdir=$(echo $basedir | sed -e 's#src#docs#')
#   name=$(echo $file | sed -e 's#.*/\([a-zA-Z]*\).yml#\1#')
#   echo $basedir
#   echo $outdir
#   echo $name
#   node node_modules/openapi3-generator/cli.js -o $outdir $file --basedir $basedir markdown
#   mv $outdir/openapi.md $outdir/$name.md
# done
#
#
# find build/src/schema -type f | while read file
# do
#   echo $file
#   basedir=$(echo $file | sed -e "s#[a-zA-Z]*.json##")
#   outdir=$(echo $basedir | sed -e 's#src#docs#')
#   name=$(echo $file | sed -e 's#.*/\([a-zA-Z]*\).json#\1#')
#   echo $basedir
#   echo $outdir
#   echo $name
#   node node_modules/openapi3-generator/cli.js -o $outdir $file --basedir $basedir markdown
#   mv $outdir/schema.md $outdir/$name.md
# done
