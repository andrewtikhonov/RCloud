assignInNamespace(x='system', system.tweaked, ns="base");
assignInNamespace(x='browseURL', browseURL.tweaked, ns="utils");
assignInNamespace(x='q', q.tweaked, ns='base');
assignInNamespace(x='edit', edit.tweaked, ns='utils');
assignInNamespace(x='fix', fix.tweaked, ns='utils');
assignInNamespace(x='fixInNamespace', fixInNamespace.tweaked, ns='utils');

assign(x='system', system.tweaked, env=.GlobalEnv);
assign(x='browseURL', browseURL.tweaked, env=.GlobalEnv);
assign(x='q', q.tweaked, env=.GlobalEnv);
assign(x='edit', edit.tweaked, env=.GlobalEnv);
assign(x='fix', fix.tweaked, env=.GlobalEnv);
assign(x='fixInNamespace', fixInNamespace.tweaked, env=.GlobalEnv);

# cleanup
rm('system.tweaked',envir=.GlobalEnv)
rm('browseURL.tweaked',envir=.GlobalEnv)
rm('q.tweaked',envir=.GlobalEnv)
rm('edit.tweaked',envir=.GlobalEnv)
rm('fix.tweaked',envir=.GlobalEnv)
rm('fixInNamespace.tweaked',envir=.GlobalEnv)

message("direct init done");

