# F2 SmartWatch features tester framework
## Description
This tool allows to control the brand DT No.I model F2 Smartwatch through a console interface.  
It is not an official tool, and has been created by analyzing the communication between the Smartwatch and the original mobile application.

## Prerequisites
The following dependencies must be installed:
```bash
 $ pip install pygatt
```

The tool has been developed using Pyhton's base features. So it should work on all pygatt compatible platforms.

## Usage
### Discovering the device MAC
In order to communicate with the Smartwatch, you first must know it MAC address.
For this purpose, you can use the following command (on Linux):
```bash
 $ sudo hcitool lescan
```

You must search for a device called F2  

### Using the framework
To launch the framework, you should use the following command:  
```bash
 $ python cli_launch.py 
```

Because the tool interacts with bluetooth devices, the system will require administrator privileges.

Once the application is opened, with the help command you can list all the functionalities.
