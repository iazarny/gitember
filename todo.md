

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


-----------------------
Proxy and proxy auth
--------------------------------
Rewrite author
------------------------

