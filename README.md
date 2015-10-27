# RCloud

##Some scripts located at https://github.com/olgamelnichuk/ansible-vcloud might be of useful installation or R Cloud and configuration of cluster.

### Read https://github.com/olgamelnichuk/ansible-vcloud/blob/master/setup_openlava.readme file first.
### setup optlava cluster
```
ansible-playbook -i hosts -c ssh -e@./secret.yml setup_openlava_cluster.yml --ask-pass
```

### setup rcloud dependencies
```
ansible-playbook -i hosts -c ssh setup_rcloud_dependencies.yml --ask-pass
```

### setup rcloud dependencies
```
ansible-playbook -i hosts -c ssh setup_rcloud_dependencies.yml --ask-pass
```

### setup java
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


