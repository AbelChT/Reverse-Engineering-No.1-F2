# Reverse engineering over DT No.1 F2
This entry pretends to make a review of the process of doing reverse engineering over the DT No.1 F2.
To guide the process, the objective will be to discover the protocol used to change the hour.
This guide assumes that you are familiar with the Gatt protocol, Wireshark, and the Android system.

## The system
The Smartwatch to be analyzed names F2 and is manufactured by DT No.1. It uses the Bluetooth Low Energy protocol to communicate with an Android application named Fundo Bracalet.

## Strategy
The first step in the process is to plan how the protocol can be discovered. This is highly dependant on the system that you have, but in this system, the strategy will be to perform some action in the application that triggers the communication associated with the process we want to discover. Then, the generated packages will be capture and analyzed to discover the communication protocol.

## Tools selection
The second step in the process is to select the tools to accomplish the task. This selection will conditionate how easy we can discover packages or understand how things work.
As we will operating over the communication between a Smartphone and a Smartwatch using Bluetooth LE, we have two main options. The first is doing a man in the middle attack to analyze the packages as they travel from one device to the other. The second is to recollect all the packages that travel from one device to the other in a period, saving them as a log, and then analyzing them.
This work will be used the second approximation. As I'm using an Android phone, it can be accomplished with a feature called Bluetooth Host Controller Interface (HCI) snoop log.
It will generate a package that can be opened later with Wireshark.

## Test design
The third step in the process is the design of focalized and useful tests. 
As we are working with a trace of the whole communication, the test must trigger the generation of identifiable packages.
As the subject is to obtain the package that changes the hour, the first thing that can be done is to change the mobile hour time and look if it changes the smartwatch hour. 
As it happens, we can create a test based on this action repeated several times to make it identifiable.
Before performing the test, we must ensure what information the log offers to us. It offers the order of the packages, the content, and the time where it was captured.
As we will change the time several times, we have several strategies there too. The first one is change is very quicky, expecting that in the log will appear a large amount of the same type of package in a short period.
The second approximation is changing the time in a regular period, and use the time where the package was captured to identify it.
The approach used was the first one changing the time 5 times setting the hours 8, 9, 10, 11, and 12 A.M. 

## Logs analysis
The first thing that we have to take into account when analyzing the packages is the protocol that is used to perform the communication. In this case, the protocol used is GATT, so we must only analyze this type of packages.
In this protocol, there are several types of packages (actions). As the communication will change the state of the smartwatch, the actions expected are "Sent write command" or "Sent write request".
Some of the package content obtained after filtering by these criteria are the following:
```
- a9 1a 00 01 04 c8
- a9 01 00 06 14 03 0e 04 0c 29 0e
- a9 0b 00 03 06 13 00 d0
```

The first thing that can be appreciated in the packages when they are filtered by these criteria is that the first byte of all of them is a9, the third byte is 00 and the second byte of packages of the same size is equal.
This can indicate to us that the second field represents the type of package and that these three packages are part of a header.

Before analyzing, I found the following interesting packages content: 
```
- a9 01 00 06 14 04 0c 08 00 00 dc
- a9 01 00 06 14 04 0c 09 00 00 dd
- a9 01 00 06 14 04 0c 0a 00 00 de
- a9 01 00 06 14 04 0c 0b 00 00 df
- a9 01 00 06 14 04 0c 0c 00 00 e0
```

As we can see, all of them start with the same fields in the headers and the eighth byte is equal to the hour we have set. Also, we can check that the last byte change too, indicating that it might be a checksum.

## Testing package
Once a package is identified, it must be tested to check if it does what we expected to.
To do this, I use Gatttool, which allows us to test individual packages and send them. 
In this case, when you send one of these packages anything happens. This is because the entire communication protocol wasn't discovered yet.
To discover the rest of the necessary packages, all of the previously captured packages were sent too. As a result, the date was changed, so the process was repeated eliminating packages selectively until the necessary packages were discovered.
