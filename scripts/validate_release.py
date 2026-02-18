import os
import sys

tag = os.getenv("TAG_NAME")

if tag:
    if not tag.startswith("v"):
        print("ERROR: Release tag must start with v (e.g. v1.0.0)")
        sys.exit(1)
    print("Valid release tag:", tag)
else:
    print("Not a release build")
