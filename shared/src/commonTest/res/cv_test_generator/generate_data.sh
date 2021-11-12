#!/bin/bash

echo Removing Numpy cache:
rm output/cache.npy

for i in {1..10}; do
    echo \#$i
    echo 'Step 1 --- Render image'
    PYTHONPATH=$(which python) blender -b chess_gen.blend --python-use-system-env --python chess_gen.py
    echo 'Step 2 --- Rename files'
    mv output/frame.jpg ../tagged_empty_boards/$i.jpg
    mv output/metadata.json ../tagged_empty_boards/$i.json
    echo 'All done'
done

rm -r output
