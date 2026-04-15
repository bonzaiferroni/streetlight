import bpy
import math
from bpy_extras import anim_utils
import render_utilities as ru

# --- Clear scene ---
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()

# --- Cube ---
bpy.ops.mesh.primitive_cube_add(size=2, location=(0, 0, 0))
cube = bpy.context.active_object
cube.name = "SpinningCube"

mat = bpy.data.materials.new(name="CubeMaterial")
mat.use_nodes = True
bsdf = mat.node_tree.nodes["Principled BSDF"]
bsdf.inputs["Base Color"].default_value = (0.2, 0.5, 0.8, 1.0)
bsdf.inputs["Metallic"].default_value = 0.3
bsdf.inputs["Roughness"].default_value = 0.4
cube.data.materials.append(mat)

# --- Animation: quarter turn for seamless loop ---
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 30
scene.render.fps = 30

cube.rotation_euler = (0, 0, 0)
cube.keyframe_insert(data_path="rotation_euler", frame=1)

cube.rotation_euler = (0, 0, math.radians(90))
cube.keyframe_insert(data_path="rotation_euler", frame=31)

# Linear interpolation (Blender 5.x channelbag API)
action = cube.animation_data.action
slot = action.slots[0]
channelbag = anim_utils.action_get_channelbag_for_slot(action, slot)
for fcurve in channelbag.fcurves:
    for kf in fcurve.keyframe_points:
        kf.interpolation = 'LINEAR'

# --- Light ---
bpy.ops.object.light_add(type='SUN', location=(5, -5, 10))
bpy.context.active_object.data.energy = 3

# --- Camera ---
cam_dist = 6
cam_angle = math.radians(30)
bpy.ops.object.camera_add(location=(0, -cam_dist * math.cos(cam_angle), cam_dist * math.sin(cam_angle)))
camera = bpy.context.active_object
camera.data.lens = 50
scene.camera = camera

constraint = camera.constraints.new(type='TRACK_TO')
constraint.target = cube
constraint.track_axis = 'TRACK_NEGATIVE_Z'
constraint.up_axis = 'UP_Y'

# --- Render ---
ru.configure_webm_output(resolution=256)
ru.render_animation()