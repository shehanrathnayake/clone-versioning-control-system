# Clone Version Control System

### Version
0.8.0

### License
Copyright &copy; 2023 Shehan Rathnayake. All Rights Reserved.<br>
This project is licensed under [MIT License](License.txt).

## Clone Installation Guide

The installation files are available for Ubuntu users in ```.deb``` file in this repository.

```sudo dpkg -i clone-0.8.0-ubuntu.deb``` to install.

See the README.txt inside the [clone-0.8.0-ubuntu-package](clone-0.7.1-ubuntu-package/) directory also.

## Clone User Guide

### Introduction
Clone is a version control software designed to manage your projects efficiently. This guide provides instructions on how to use Clone to manage your code repositories effectively.

```clone``` - To see whether Clone has been installed successfully

### Initializing a Clone Repository
To initialize a Clone repository in the specified target directory, run the following command:

```clone start```

Run this command only once to create the repository in the target directory.

### Using Other Commands
After initializing the repository, you can use various other commands for different functionalities as needed in your development workflow.

```clone [-h | --help]``` - To see the command list<br>
```clone [-v | --version]``` - To see the version<br>

```clone show``` - To see the current status of files<br>
```clone make``` - Getting ready all the files for saving<br>
```clone save``` - Saving a snapshot of the current project<br>
```clone log``` - Displaying all the clones saved<br>
```clone activate <hashcode>``` - Traversing history through saved clones<br>

## Sample Demonstrations

### Through The Terminal
![Commands demonstration through terminal](img/demo-img-terminal.png)

### Through VSCode
![Commands demonstration through vscode 1](img/demo-img-vscode1.png)
![Commands demonstration through vscode 2](img/demo-img-vscode2.png)
![Commands demonstration through vscode 3](img/demo-img-vscode3.png)
![Commands demonstration through vscode 4](img/demo-img-vscode4.png)
![Commands demonstration through vscode 5](img/demo-img-vscode5.png)
![Commands demonstration through vscode 6](img/demo-img-vscode6.png)

