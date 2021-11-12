#!/bin/bash

PYTHONPATH=$(which python) blender chess_gen.blend --python-use-system-env
