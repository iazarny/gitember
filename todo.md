

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



            /*ORIGINAL DO NOT REMOVE Platform.runLater(
                    () -> {
                        RemoteOperationValue res = remoteRepositoryOperation(
                                () -> MainApp.getRepositoryService().cloneRepository(
                                        dialogResult.get().getFirst(),
                                        dialogResult.get().getSecond(),
                                        login,
                                        pwd,
                                        new DefaultProgressMonitor(d -> updateProgressBar(d))
                                )
                        );
                        openRepository((String) res.getValue());
                    }
            );*/


```