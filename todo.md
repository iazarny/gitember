

# View binary  file
View binary file shall be done using OS, something line this

```
if (GitemberUtil.isBinaryFile(temp)) {
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(temp.toURI()); //TODO bad idea, need to rethink
                } catch (IOException e) {
                    e.printStackTrace(); //TODO error dialog

                }
            }).start();
        }
}
```

```
 //all files in revision
    /*RevTree tree = revCommit.getTree();
    try (TreeWalk treeWalk = new TreeWalk(repository)) {
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        while (treeWalk.next()) {
            System.out.println(">>> found: " + treeWalk.getPathString());
        }

    }*/
```
history
---------------------
https://gist.github.com/floralvikings/10290131



repo usr with username
-----------------------
Proxy and proxy auth
--------------------------------
Rewrite author
------------------------
SSh protocol support
-------
Compress db
---------------------
Depict result of operation
--------------------
Statistics
-----------------------
Search
------
Funcy about
-----------

