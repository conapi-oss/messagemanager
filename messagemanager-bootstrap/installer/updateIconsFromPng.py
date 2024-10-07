import os
import glob
from PIL import Image




def resize_image(input_file, output_file, width, height):
        img = Image.open(input_file)
        img_resize = img.resize((width, height), Image.LANCZOS)
        img_resize.save(output_file)


input = 'C:\\Users\\Stefan\\My Drive\\conapi.at website\\2024-Rebranding\\Sublogo\\Sublogo\\PNG\\sublogo_gradient_on_light_background.png' #sublogo_darkpurple_with_lime.png'
output_dir = 'C:\\dev\\git\\conapi\\messagemanager-oss\\messagemanager-bootstrap\\src\\main\\resources\\icons\\'
os.makedirs(output_dir, exist_ok=True)

#output_file = os.path.join(output_dir, os.path.basename(input))
#resize_image(input, output_file, 128, 128)


output_file = 'C:\\dev\\git\\conapi\\messagemanager-oss\\messagemanager-app\\src\\main\\resources\\images\\messagemanager-icon.png'
resize_image(input, output_file, 16, 16)


output_file = os.path.join(output_dir, 'conapi_16x16.png')
resize_image(input, output_file, 16, 16)

output_file = os.path.join(output_dir, 'conapi_32x32.png')
resize_image(input, output_file, 32, 32)

output_file = os.path.join(output_dir, 'conapi_48x48.png')
resize_image(input, output_file, 48, 48)

output_file = os.path.join(output_dir, 'conapi_64x64.png')
resize_image(input, output_file, 64, 64)

output_file = os.path.join(output_dir, 'conapi_128x128.png')
resize_image(input, output_file, 128, 128)

output_file = os.path.join(output_dir, 'messagemanager-icon-large.png')
resize_image(input, output_file, 128, 128)

output_file = os.path.join(output_dir, 'messagemanager-icon-medium.png')
resize_image(input, output_file, 64, 64)

output_file = os.path.join(output_dir, 'messagemanager-icon-small.png')
resize_image(input, output_file, 32, 32)

output_file = os.path.join(output_dir, 'messagemanager-icon-tiny.png')
resize_image(input, output_file, 16, 16)
