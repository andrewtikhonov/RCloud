# load libraries
#

.PrivateEnv <- new.env(parent = .GlobalEnv)

assign('installAndLoadLibrary', function(libname) {
    status = try( library(libname, character.only=T) )
    if (inherits(status, 'try-error')) {
        install.packages(libname, repos="http://mirrors.ebi.ac.uk/CRAN")
        library(libname, character.only=T)
    }
}, env=.PrivateEnv)

library(rJava)

.PrivateEnv$installAndLoadLibrary('JavaGD')
.PrivateEnv$installAndLoadLibrary('tools')
.PrivateEnv$installAndLoadLibrary('svMisc')

try(.jinit(), silent = TRUE)

# hide original stuff

assign('q', q , env=.PrivateEnv);
assign('dir', dir , env=.PrivateEnv);
assign('ls', ls , env=.PrivateEnv);
assign('objects', objects , env=.PrivateEnv);

assign('help', help , env=.PrivateEnv);
assign('q', q , env=.PrivateEnv);

assign('setwd', setwd , env=.PrivateEnv);
assign('getwd', getwd , env=.PrivateEnv);

assign('dev.set', dev.set , env=.PrivateEnv);
assign('dev.off', dev.off , env=.PrivateEnv);
assign('dev.cur', dev.cur , env=.PrivateEnv);
assign('dev.list', dev.list , env=.PrivateEnv);
assign('dev.copy', dev.copy , env=.PrivateEnv);
assign('graphics.off', graphics.off , env=.PrivateEnv);
assign('system', system , env=.PrivateEnv);
assign('browseURL', browseURL , env=.PrivateEnv);

assign('edit', edit , env=.PrivateEnv);
assign('fix', fix , env=.PrivateEnv);
assign('fixInNamespace', fixInNamespace , env=.PrivateEnv);

try(assign('win.graph', win.graph , env=.PrivateEnv),silent=TRUE);
try(assign('x11', x11 , env=.PrivateEnv),silent=TRUE);
try(assign('X11', X11 , env=.PrivateEnv),silent=TRUE);

Sys.setenv(PATH=paste(Sys.getenv("PATH"),  paste(
    ":/ebi/microarray/home/biocep/local/lib64/R/bin",
    "/ebi/microarray/home/biocep/local/bin",
    "/ebi/microarray/sw/bin",
    "/ebi/research/software/Linux_x86_64/opt/java/jdk1.6/bin",
    "/usr/local/bin",
    "/bin",
    "/usr/bin",
    "/usr/X11R6/bin:", sep=":")))

#   B I O C
#
try(source("http://mirrors.ebi.ac.uk/bioconductor/biocLite.R"), silent = TRUE)

message("initial init done");