
#   P R I V A T E   F U N C T I O N S
#

assign('javapager', function (file, header = rep("", nfiles), title = "R Information", 
    delete.file = FALSE, pager = getOption("pager"), encoding = "") {
	.jcall("uk/ac/ebi/rcloud/server/RListener",,"pager",
	    as.character(file), as.character(header), as.character(title),  as.character(delete.file) )
}, env=.PrivateEnv)

options(pager=.PrivateEnv$javapager)

assign('notifyJavaListeners', function(parameters)  {
	.jcall( obj="uk/ac/ebi/rcloud/server/RListener" , "V" ,"notifyJavaListeners", parameters )
}, env=.PrivateEnv)

assign('nop', function()  {
	return(invisible(NULL))
}, env=.PrivateEnv)

assign('dev.broadcast',  function () {
	temp_dev_list<-.PrivateEnv$dev.list();
 	if (!is.null(temp_dev_list)) {
		temp_dev_cur<-.PrivateEnv$dev.cur();
		for (i in 1:length(temp_dev_list)) {
			if (temp_dev_list[i]!=temp_dev_cur) {
				.PrivateEnv$dev.copy(which=temp_dev_list[i]);
			}
			.PrivateEnv$dev.set(temp_dev_cur);
		}
	}
	return(invisible(NULL));
}, env=.PrivateEnv)

assign('contains', function (str, vec) {
	result<-FALSE;
	for (i in 1:length(vec)) {
		if (vec[i]==str) {
			result<-TRUE;
			break;
		}
	}
	result
}, env=.PrivateEnv);


assign('initPackageInstaller', function(repo) {
    #try({ zzz <- repo; attr(zzz, "names") <- "CRAN"; options(repos = zzz); rm(zzz) })
    #source(http://mirrors.ebi.ac.uk/bioconductor/2.4/data/)
}, env=.PrivateEnv)

assign('getPackagesInfo', function() {
    d <- matrix(character(0L), nrow = 0L, ncol = 2L)

    for (l in .libPaths()) {
		a <- .packages(all.available = TRUE, lib.loc = l)
        for (i in sort(a)) {
            file <- system.file("Meta", "package.rds", package = i, lib.loc = l)
			desc <- .readRDS(file)$DESCRIPTION
			d <- rbind(d, c(desc["Package"], desc["Title"]))
        }
    }
    d
}, env=.PrivateEnv)

assign('getClass', function(x) {
        r <- try(do.call("class", list(as.name(x))), silent = TRUE)
        if (inherits(r, "try-error")) {
            "undefined"
        } else {
            r
        }
}, env=.PrivateEnv)


assign('doComplete', function(w) {
    result <- utils:::.win32consoleCompletion(w, nchar(w))
    names <- unlist(strsplit(result[['comps']], " "))

    if (length(names) > 0) {
        outcome <- list()
        outcome[['names']] <- unlist(sapply(names, function(x){ .PrivateEnv$getClass(x)[[1]] }))
        outcome[['addition']] <- result[['addition']]

        outcome
    } else {
        NULL;
    }
}, env=.PrivateEnv)


assign('deparseArgumentOld', function(arg) {
    try({result <- arg}, silent=TRUE)
    if (exists("result")) {
        results <- attr(result, 'source')
        if (is.null(results)) {
            return(deparse(result))
        } else {
            return(results)
        }
    }
    NULL
}, env=.PrivateEnv)

assign('deparseArgument', function(arg){
    result <- try({arg}, silent=TRUE)

    if (inherits(result, 'try-error')) {
        getAnywhereObject <- do.call(getAnywhere,
            list(as.character(substitute(arg))));

        if (length(getAnywhereObject$objs) > 0) {
            result <- getAnywhereObject$objs[[1]]
        }
    }

    if (exists("result")) {
        results <- attr(result, 'source')
        if (is.null(results)) {
            return(deparse(result))
        } else {
            return(results)
        }
    }

    NULL
}, envir=.PrivateEnv)


assign('getServerName', function() {
    .jcall(obj="uk/ac/ebi/rcloud/server/RListener", "[Ljava/lang/String;", "getServerName")
}, envir=.PrivateEnv)


#   T W E A K E D   F U N C T I O N S
#
#
#

# namespace:base
assign('system.tweaked', function (command, intern = FALSE, ignore.stdout = FALSE, ignore.stderr = FALSE,
    wait = TRUE, input = NULL, show.output.on.console = TRUE,
    minimized = FALSE, invisible = TRUE)
{
    direct = FALSE

    if (!missing(show.output.on.console) || !missing(minimized) ||
        !missing(invisible))
        message("arguments 'show.output.on.console', 'minimized' and 'invisible' are for Windows only")
    if (!is.logical(intern) || is.na(intern))
        stop("'intern' must be TRUE or FALSE")
    if (!is.logical(ignore.stdout) || is.na(ignore.stdout))
        stop("'ignore.stdout' must be TRUE or FALSE")
    if (!is.logical(ignore.stderr) || is.na(ignore.stderr))
        stop("'ignore.stderr' must be TRUE or FALSE")
    if (!is.logical(wait) || is.na(wait))
        stop("'wait' must be TRUE or FALSE")
    if (ignore.stdout)
        command <- paste(command, ">/dev/null")
    if (ignore.stderr)
        command <- paste(command, "2>/dev/null")
    if (!is.null(input)) {
        if (!is.character(input))
            stop("'input' must be a character vector or 'NULL'")
        f <- tempfile()
        on.exit(unlink(f))
        writeLines(input, f)
        command <- paste(command, "<", f)
    }
    if (!wait && !intern)
        command <- paste(command, "&")


    if (direct) {
        .Internal(system(command, intern))
    } else {
        result <- .jcall( obj="uk/ac/ebi/rcloud/server/RListener" , "[Ljava/lang/String;" ,"exec",
            command, as.character(intern) );

        if (!intern) {
            result <- as.numeric(result);
        }

        if (invisible) {
            return(invisible(result));
        } else {
            return(result);
        }
    }
}, envir=.GlobalEnv)


#namespace:utils

assign('browseURL.tweaked', function (url, browser = getOption("browser"), encodeIfNeeded = FALSE)
{
    if (browser == "RCloud") {
        result <- unlist(strsplit(url, "/"))
        tempurl <- paste(c("", result[4:length(result)]), collapse="/")

        .jcall( obj="uk/ac/ebi/rcloud/server/RListener" , "[Ljava/lang/String;" ,
        "help", as.character(tempurl)[1] );
        return(invisible(NULL));
    } else {
        .PrivateEnv$browseURL(url = url, browser = browser, encodeIfNeeded = encodeIfNeeded);
    }
}, envir=.GlobalEnv)

# namespace:base
assign('q.tweaked', function (save = "default", status = 0, runLast = TRUE) {
	.jcall( obj="uk/ac/ebi/rcloud/server/RListener" ,
	    "[Ljava/lang/String;" ,"q", as.character(save) , as.character(status) , as.character(runLast) );
	return(invisible(NULL));
}, envir=.GlobalEnv)

#namespace:utils
assign('edit.tweaked', function(x) {
    .jcall( obj="uk/ac/ebi/rcloud/server/RListener",
        "[Ljava/lang/String;" ,"edit", deparse(substitute(x)) ); return(invisible(NULL));
}, envir=.GlobalEnv)


#namespace:utils
assign('fix.tweaked', function(x, ...) {
    .jcall( obj="uk/ac/ebi/rcloud/server/RListener",
        "[Ljava/lang/String;" ,"edit", deparse(substitute(x)) ); return(invisible(NULL));
}, envir=.GlobalEnv)


#namespace:utils
assign('fixInNamespace.tweaked', function (x, ns, pos = -1, envir = as.environment(pos), ...) {
    subx <- substitute(x)
    if (is.name(subx))
        subx <- deparse(subx)
    if (!is.character(subx) || length(subx) != 1)
        stop("'fixInNamespace' requires a name")
    if (missing(ns)) {
        nm <- attr(envir, "name", exact = TRUE)
        if (is.null(nm) || substring(nm, 1, 8) != "package:")
            stop("environment specified is not a package")

        ns <- nm
        # ns <- asNamespace(substring(nm, 9))
    }
    # else ns <- asNamespace(ns)
    # x <- edit(get(subx, envir = ns, inherits = FALSE), ...)
    # assignInNamespace(subx, x, ns)

    .jcall( obj="uk/ac/ebi/rcloud/server/RListener",
        "[Ljava/lang/String;" ,"edit", paste(ns,":::", subx, sep="") );

    return(invisible(NULL));
}, envir=.GlobalEnv)


# load package, try to detach and unload it before loading
assign('library.reload', function(package,...) {
   try({detach(paste("package",substitute(package),sep=":"), unload=TRUE, character.only=TRUE, ...)}, silent=TRUE);

   library(as.character(substitute(package)), character.only=TRUE, ...);
}, envir=.GlobalEnv)

#   S E T T I N G S   &   O P T I O N S
#
#
#

# default browser
options('browser' = 'RCloud')

# default bioconductor mirror - our
options(BioC_mirror =  "http://mirrors.ebi.ac.uk/bioconductor")

# default cran mirror - our
options(repos = c(CRAN = "http://mirrors.ebi.ac.uk/CRAN"))

# type of package
options(pkgType = 'source')

# max number of lines in the R output
options(max.print = 1000)

# default type of help pages 
options(help_type = 'html')

# complete files
utils::rc.settings(files=TRUE)

message("reload init done");