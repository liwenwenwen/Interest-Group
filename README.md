# Interest Groups
Using the Internet domain sockets, Interest Groups implements a network application that supports interest-based discussion groups.

> CSE 310 [Computer Networks](https://www.cs.stonybrook.edu/students/Undergraduate-Studies/courses/CSE310) course project

Authors: [Liwen Fan](https://github.com/liwenwenwen),     [Jia Sheng Ma](https://github.com/majiasheng),     [Melanie Lin](https://github.com/melanie0926)

# Table of Contents
* [What it does](#intro)
* [Documentation](#documentation)
* [Rules of the Game](#rules)
* [Demo of the Game](#demo)
* [How we built it](#built)
* [Challenges we ran into](#challenges)
* [Accomplishments that we're proud of](#accomplishments)
* [What we've learned](#learned)
* [What's next for Interest Groups](#next)

## What it does <a id="intro"> </a>

Using the TCP socket, Interest Groups implements a network application that supports interest-based discussion groups. 

The implementation consists of a client program and a server program. The client program allows an application user to login to the application, browse existing discussion groups, subscribe to those groups that are of interest, and read and write posts in a subscribed group. The server program maintains all the discussion groups, updates the posts in each group, and interacts with clients to support user activities.  

## Documentation <a id="documentation"> </a>

### Quick Start

**Browse to the server folder**

`$ cd InterestGroup_S/`

**Create a new build folder**

`$ mkdir build`

**Compile with**

`$ javac -cp lib/jackson-core-2.8.2.jar:. src/*/*.java -d build/`

**Browse to the build folder**

`$ cd build`

**Run the project with**

`$ java -cp ../lib/jackson-core-2.8.2.jar:. interest_group_s.InterestGroup_Server HOSTMACHINE PORTNUMBER`

### More Information

* [System Documentation](https://github.com/melanie0926/Interest-Groups/files/671443/System.Documentation.pdf)
* [Testing Documentation](https://github.com/melanie0926/Interest-Groups/files/671444/Testing.Documentation.pdf)
* [User Documentation](https://github.com/melanie0926/Interest-Groups/files/671446/User.Documentation.pdf)

## Rules of the Program <a id="rules"> </a>

All discussion groups are hosted at a single server. Each user must use an unique ID to access this single server to participate in discussion groups.

## Demo of the Program <a id="demo"> </a>



## How we built it <a id="built"> </a>

We decided to use the TCP protocol because all data transferred between the server program and the client program, including login credentials, browsing history, and discussion groups information, cannot tolerate data loss. We also decided to use JSON to store these data because JSON organizes its content in key-value pairs.

Next, we designed the TCP protocol involved for transferring data, as well as other supporting elements such as how discussion groups and user posts are formatted, stored, and accessed and how user history is formatted and maintained. 

Finally, we wrote handlers on the client program for commands that do not involve saving or retrieving data from the server. This approach reduces the number of messages exchanged between the client and the server, and may allow the server to more efficiently support a large number of clients.

## Challenges we ran into <a id="challenges"> </a>



## Accomplishments that we're proud of <a id="accomplishments"> </a>


## What we've learned <a id="learned"> </a>


## What's next for Interest Groups <a id="next"> </a>

Interest Groups aims to provide a multi-server environment







