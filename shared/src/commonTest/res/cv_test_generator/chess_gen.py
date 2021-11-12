import bpy, bpy_extras, mathutils
import random
import math
import pandas

SW = 0.1185  # Square width
BW = 0.95  # Board width

MAX_OFFSET = 0.02  # Maximum piece distance from square center
CAM_DIST_RANGE = (2.0, 3.0)
CAM_HEIGHT_RANGE = (0.5, 3.0)

IMAGE_OUTPUT = bpy.path.abspath('//output/frame.jpg')
CORNER_OUTPUT = bpy.path.abspath('//output/metadata.json')

CAM = bpy.data.objects['camera']

PIECE_WEIGHTS = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]

def get_square_center(x: int, y: int) -> (float, float):
    return ((x+0.5)*SW - BW/2, (y+0.5)*SW - BW/2, -0.001)

def world_to_screen(p_3d: mathutils.Vector):
    scene = bpy.context.scene
    p_2d = bpy_extras.object_utils.world_to_camera_view(scene, CAM, p_3d)
    
    render_scale = scene.render.resolution_percentage / 100
    render_size = (int(scene.render.resolution_x * render_scale),
                   int(scene.render.resolution_y * render_scale))
    p_2d = (p_2d[0] * render_size[0],
            render_size[1] - p_2d[1] * render_size[1])
    return p_2d


## Delete any piece instances
for piece in bpy.data.collections['pieces'].objects:
    bpy.data.objects.remove(piece, do_unlink=True)

## Position pieces
"""
board = [[(get_square_center(x, y), None) for x in range(8)] for y in range(8)]
coords = [(x, y) for x in range(8) for y in range(8)]
pieces_set = ([None] +
              list(bpy.data.collections['white_pieces'].objects) +
              list(bpy.data.collections['black_pieces'].objects))
counts = [0 for _ in range(len(pieces_set))]

for (x, y) in coords:
    i = random.choices(range(len(pieces_set)), weights=PIECE_WEIGHTS)[0]
    piece = pieces_set[i]
    if piece is None:
        continue
    piece_1 = piece.copy()
    bpy.data.collections['pieces'].objects.link(piece_1)
    piece_1.name = f'{piece.name}_{counts[i]}'
    pos = board[y][x][0]
    board[y][x] = (pos, piece_1.name)
    piece_1.location = (pos[0] + random.uniform(-MAX_OFFSET, MAX_OFFSET),
                        pos[1] + random.uniform(-MAX_OFFSET, MAX_OFFSET),
                        pos[2])
    piece.rotation_euler = (0, 0, random.uniform(0, 2*math.pi))
    counts[i] += 1
"""
## Position camera
r = random.uniform(CAM_DIST_RANGE[0], CAM_DIST_RANGE[1])
theta = random.uniform(0, 2*math.pi)
pos = (r*math.cos(theta), r*math.sin(theta))
CAM.location = (pos[0],
                pos[1],
                random.uniform(CAM_HEIGHT_RANGE[0], CAM_HEIGHT_RANGE[1]))

## Render and save image
bpy.context.scene.render.filepath = IMAGE_OUTPUT
bpy.ops.render.render(write_still=True)

## Save the location of corners
metadata = {'corners': {}}
for c in ['top_left', 'top_right', 'bottom_left', 'bottom_right']:
    corner_name = 'corner_' + c
    corner = bpy.data.objects[corner_name]
    metadata['corners'][c] = world_to_screen(corner.location)

df = pandas.DataFrame(metadata)
df.to_json(CORNER_OUTPUT)

## Save the location and content of each square
"""
rows = []
for y, row in enumerate(board):
    for x, (pos, name) in enumerate(row):
        pos = mathutils.Vector(pos)
        screen_pos = world_to_screen(pos)
        if name is None:
            rows.append({'screen_x': screen_pos[0],
                         'screen_y': screen_pos[1]})
            continue
        words = name.split('_')
        piece_color = words[0]
        piece_type = words[1]
        if len(words) > 2:
            piece_id = int(words[2])
        else:
            piece_id = 0
        
        rows.append({'screen_x': screen_pos[0],
                     'screen_y': screen_pos[1],
                     'piece_type': piece_type,
                     'piece_color': piece_color,
                     'piece_id': piece_id})
pieces = pandas.DataFrame(rows)
pieces.to_csv(PIECES_OUTPUT)
"""

print('Done')
