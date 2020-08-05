from sikuli import *

import sys
import os

def set_class_path():
    path_script = getBundlePath()
    dir_script = os.path.dirname( path_script )
    path_lib = dir_script + "/keycast/build/libs/keycast.jar"
    
    sys.path.append( path_lib )

def launch():
    from com.whitemagicsoftware.keycast import KeyCast
    kc = KeyCast()
    kc.show()

def main():
    set_class_path()
    launch()
   

if __name__ == "__main__":
    main()
