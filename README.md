# RCloud

## Overview

R Cloud is a distributed framework for computational research. R Cloud runs numerous servers in the background that researchers utilize for their needs, mainly for parallel processing of massive datasets. Part of the R Cloud is the R Cloud Workbench, an IDE, feature-rich client application for research and development in the R Cloud environment. R Cloud Workbench communicates with the backend R Cloud servers.

## Running service
The main R Cloud service instance is running at EBI (European Bioinformatics Institute). R Cloud is provided to research community as a computational service free to use. Academic research institutes in collaboration with EBI are granted more computational resources - CPU and memory - than public access instances. Please visit the web site of the R Cloud at EBI http://www.ebi.ac.uk/Tools/rcloud for more details and access to the R Cloud service.


## Installation

Some scripts located at https://github.com/olgamelnichuk/ansible-vcloud might be of use during installation of the R Cloud and configuration of cluster.

### Setup optlava cluster
Read https://github.com/olgamelnichuk/ansible-vcloud/blob/master/setup_openlava.readme file first.
```
ansible-playbook -i hosts -c ssh -e@./secret.yml setup_openlava_cluster.yml --ask-pass
```

### Setup R Cloud dependencies
These are needed for installation of R
```
ansible-playbook -i hosts -c ssh setup_rcloud_dependencies.yml --ask-pass
```

### Setup java
R Cloud is a framework written in Java.
```
ansible-playbook -i hosts -c ssh setup_rcloud_java.yml --ask-pass
```

### R Cloud needs to be installed on a network file system that is mounted on cluster machines
```
ansible-playbook -i hosts -c ssh setup_rcloud_nfs_export.yml --ask-pass
ansible-playbook -i hosts -c ssh -e@./setup_rcloud_nfs_vars.yml setup_rcloud_nfs_mount.yml --ask-pass
```

### define rcloud users that own files and run the software
```
ansible-playbook -i hosts -c ssh -e@./setup_rcloud_user_vars.yml setup_rcloud_users.yml --ask-pass
```

### change ownership of the folders
```
ansible-playbook -i hosts -c ssh setup_rcloud_chown_home_dirs.yml --ask-pass
```

### Install R
Please follow instuctions from the official R web site https://www.r-project.org

### Compile and install, the R Cloud
```
git pull
mvn package
```

Deploy rcloud-web.war to a Tomcat container

Deploy rcloud-server.jar to the location where R Cloud server will be kept
```
mv rcloud-server.jar /mnt/rcloud/service/
```

### Install postgress database
Follow instuctions from http://www.postgresql.org/

### Populate the database
Sample R Cloud database for EMIF project can be found from https://github.com/andrewtikhonov/RCloud/blob/master/testdb.txt

