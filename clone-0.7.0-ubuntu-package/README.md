# Clone Version Control System

## Clone Installation Guide

The installation files are available for Ubuntu users in ```.deb``` file and ```.tar.gz``` in the directory of [clone-0.7.0-ubuntu-package](clone-0.7.0-ubuntu-package/) in this repository.

### ```.deb``` file installation
Use ```sudo dpkg -i clone-0.7.0-ubuntu.deb``` to install.

### ```.tar.gz``` file installation
This ```.tar``` file includes ```clone-vcs``` directory and ```clone``` file. Simply place ```clone-vcs``` in the ```/opt``` directory and ```clone``` file in the ```/usr/bin``` directory.

See the README.md inside [clone-0.7.0-ubuntu-package](clone-0.7.0-ubuntu-package/) also.

## Clone User Guide

### Introduction
Clone is a version control software designed to manage your projects efficiently. This guide provides instructions on how to use Clone to manage your code repositories effectively.

### Running Clone
To run the Clone software, use the following command in your terminal:

```java Clone "path/to/directory"```

Replace "path/to/directory" with the actual path to your desired target directory.

### Initializing a Clone Repository
To initialize a Clone repository in the specified target directory, run the following command:

```clone start```

Run this command only once to create the repository in the target directory.

### Using Other Commands
After initializing the repository, you can use various other commands for different functionalities as needed in your development workflow.

```clone make``` => Getting ready all the files for saving<br>
```clone save``` => Saving a snapshot of the current project<br>
```clone log``` => Displaying all the clones saved<br>
```clone activate hashcode``` => Traversing history through saved clones<br>

