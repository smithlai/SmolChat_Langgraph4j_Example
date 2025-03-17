# Updating the `llama.cpp` submodule

The project uses `llama.cpp` as a Git submodule that has to be updated to the latest commit. The submodule is located at `llama.cpp` in the root directory of the project.

To update the `llama.cpp` submodule, follow these steps:

```bash
# Start running the following commands from the root of this project
cd llama.cpp
git pull origin master
cd ..
git status # Check if the submodule has been updated
```